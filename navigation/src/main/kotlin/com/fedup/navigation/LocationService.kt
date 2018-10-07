package com.fedup.navigation

import com.fedup.common.*
import com.google.maps.*
import com.google.maps.model.*
import org.springframework.stereotype.*

data class HowFar(val distance: Distance, val duration: Duration)
data class UserWithDistance(val userId: UserId, val distanceMessage: String)

/**
 * Subscribes to the user-location stream, where it listens for [NearbyDriversRequested] events. Upon receiving one,
 * finds the drivers and publishes [DriversLocated] event to the shipping stream (keyed by [TrackingId])
 */
@Component
class LocationService(
    private val userLocationRepository: UserLocationRepository,
    private val googleMapsApiKey: String
) {
    fun recordUserLocation(userLocation: UserLocation) {
        userLocationRepository.saveUserLocation(userLocation)
    }

    fun closestDrivers(location: Location): List<UserWithDistance> {
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
            .sortedBy { it.first.duration.inSeconds }
            .take(5)
            .map { UserWithDistance(it.second.userId, "${it.first.duration.humanReadable} (${it.first.distance.humanReadable})") }
    }


}

/**
 * Sits on top of user-location stream-backed KTable
 */
@Component
class UserLocationRepository {
    fun findAvailableDrivers(): List<UserLocation> {
        return listOf(UserLocation("1", Location(37.7724868,-122.4166086)), UserLocation("2", Location(37.7339012,-122.4194585)), UserLocation("3", Location(37.7486923,-122.4186179)))
    }

    fun saveUserLocation(userLocation: UserLocation) {

    }
}
