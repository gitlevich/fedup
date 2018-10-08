package com.fedup.navigation

import com.fedup.common.*
import com.fedup.common.machinery.*
import com.fedup.navigation.LocationService.Companion.SERVICE_APP_ID
import com.google.maps.*
import com.google.maps.model.*
import org.apache.kafka.common.serialization.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.*
import org.springframework.boot.autoconfigure.*
import org.springframework.context.annotation.*

data class HowFar(val distance: Distance, val duration: Duration)
data class UserWithDistance(val userId: UserId, val distanceMessage: String)

/**
 * Owns (is a single writer to) user-location stream.
 * Subscribes to the command stream, where it listens for [NearbyDriversRequested] events. Upon receiving one,
 * finds the drivers and publishes [DriversLocated] event to the user-location stream.
 */
@SpringBootApplication
@PropertySource("/application.properties")
class LocationService(
    @Value("\${googlemaps.api.key}") private val googleMapsApiKey: String,
    private val streamsConfig: StreamsConfig,
    private val userLocationRepository: UserLocationRepository
) {
    fun recordUserLocation(userLocation: UserLocation) {
        userLocationRepository.saveUserLocation(userLocation)
    }

    fun processStreams(): KafkaStreams {
        val builder = StreamsBuilder()
        val locationRequests = builder
            .stream(locationRequests.name, Consumed.with(locationRequests.keySerde, locationRequests.valueSerde))

        locationRequests
            .mapValues {location -> findDriversNear(location) }
            .to(availableDrivers.name, Produced.with(availableDrivers.keySerde, availableDrivers.valueSerde))

        return KafkaStreams(builder.build(), streamsConfig)
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
        const val SERVICE_APP_ID = "LocationService"
        val locationRequests = Topic("location-requests", Serdes.String(), CustomSerdes.LocationSerde())
        val availableDrivers = Topic("available-drivers", Serdes.String(), CustomSerdes.UsersWithDistanceSerde())
    }
}


@Configuration
class ApConfig {
    @Bean fun googleApiKey(@Value("\${googlemaps.api.key}") apiKey: String): String = apiKey

    @Bean fun streamsConfig(@Value("\${kafka.bootstrap.servers}") bootstrapServers: String,
                            @Value("\${kafka.state.dir}") stateDir: String,
                            @Value("\${kafka.enableEOS}") enableEOS: Boolean): StreamsConfig =
        createStreamsConfig(SERVICE_APP_ID, bootstrapServers, stateDir, enableEOS)
}

fun main(args: Array<String>) {
    runApplication<LocationService>(*args)
}


