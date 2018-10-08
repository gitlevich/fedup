package com.fedup.navigation

import com.fedup.shared.*
import com.fedup.shared.machinery.*
import com.fedup.shared.machinery.Service
import com.google.maps.*
import com.google.maps.model.*
import org.apache.kafka.common.serialization.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.*
import org.slf4j.*
import org.springframework.beans.factory.annotation.*
import org.springframework.stereotype.*

data class HowFar(val distance: Distance, val duration: Duration)
data class UserWithDistance(val userId: UserId, val distanceMessage: String)

/**
 * Owns (is a single writer to) user-location stream.
 * Subscribes to the command stream, where it listens for [NearbyDriversRequested] events. Upon receiving one,
 * finds the drivers and publishes [DriversLocated] event to the user-location stream.
 */
@Component
class LocationService(
    @Value("\${googlemaps.api.key}") private val googleMapsApiKey: String,
    private val kafkaConfig: KafkaStreamsConfig,
    private val userLocationRepository: UserLocationRepository
) : Service {
    val locationRequests = Topic("location-requests", Serdes.String(), CustomSerdes.commandSerde)
    val availableDrivers = Topic("available-drivers", Serdes.String(), CustomSerdes.driversLocated)
    private var streams: KafkaStreams? = null

    override fun start() {
        streams = buildStream().also { it.start() }
        logger.info("Started location service")
    }

    override fun stop() {
        streams?.close()
    }

    fun recordUserLocation(userLocation: UserLocation) {
        userLocationRepository.saveUserLocation(userLocation)
    }

    private fun buildStream(): KafkaStreams {
        val builder = StreamsBuilder()

        val locationRequests = builder.stream(
            locationRequests.name,
            Consumed.with(locationRequests.keySerde, locationRequests.valueSerde)
        )

        locationRequests
            .mapValues { request ->
                DriversLocated(
                    request.trackingId,
                    findDriversNear(request.location)
                )
            }
            .to(
                availableDrivers.name,
                Produced.with(availableDrivers.keySerde, availableDrivers.valueSerde)
            )

        return KafkaStreams(builder.build(), kafkaConfig.props)
    }


    /**
     * Finds at most max drivers closest to the specified location.
     */
    fun findDriversNear(location: Location, maxDriversToFind: Int = 5): List<UserWithDistance> {
        val availableDrivers = userLocationRepository.findAvailableDrivers()
        val driverLocations = availableDrivers
            .map { it.location.toString() }
            .toTypedArray()

        val context = GeoApiContext.Builder()
            .apiKey(googleMapsApiKey)
            .build()

        val distanceMatrix = DistanceMatrixApi
            .getDistanceMatrix(context, arrayOf(location.toString()), driverLocations)
            .await()

        return distanceMatrix.rows.first().elements
            .map { HowFar(it.distance, it.duration) }
            .zip(availableDrivers)
            .asSequence()
            .sortedBy { it.first.duration.inSeconds }
            .take(maxDriversToFind)
            .map { UserWithDistance(it.second.userId, "${it.first.duration.humanReadable} (${it.first.distance.humanReadable})") }
            .toList()
    }

    companion object {
        val logger = LoggerFactory.getLogger(LocationService::class.java)
    }
}


