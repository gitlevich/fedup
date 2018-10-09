package com.fedup.location

import com.fedup.shared.machinery.*
import com.fedup.shared.machinery.KafkaService
import com.fedup.shared.protocol.Topics.availableDrivers
import com.fedup.shared.protocol.Topics.locationRequests
import com.fedup.shared.protocol.location.*
import com.google.maps.*
import com.google.maps.model.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.*
import org.slf4j.*
import org.springframework.beans.factory.annotation.*
import org.springframework.stereotype.*

data class HowFar(val distance: Distance, val duration: Duration)

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
) : KafkaService {
    private var streams: KafkaStreams? = null

    override fun start() {
        streams = buildStream(kafkaConfig).also { it.start() }
        userLocationRepository.start() // this is really weird, I need to find a better way to define streams... maybe repository is not a possible pattern here?
        logger.info("Started location service")
    }

    override fun stop() {
        streams?.close()
        userLocationRepository.stop()
    }

    fun recordUserLocation(userLocation: UserLocation) {
        userLocationRepository.saveUserLocation(userLocation)
    }

    fun buildStream(kafkaStreamsConfig: KafkaStreamsConfig): KafkaStreams {
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

        return KafkaStreams(builder.build(), kafkaStreamsConfig.props)
    }


    /**
     * Finds at most max drivers closest to the specified location.
     */
    fun findDriversNear(location: Location, maxDriversToFind: Int = 5): List<UserWithDistance> {
        val availableDrivers = userLocationRepository.findAllDrivers()
        if(availableDrivers.isEmpty()) return emptyList()

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
        val logger: Logger = LoggerFactory.getLogger(LocationService::class.java)
    }
}


