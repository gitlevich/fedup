package com.fedup.location

import com.fedup.shared.protocol.location.*
import com.google.maps.*
import com.google.maps.model.*
import org.springframework.beans.factory.annotation.*
import org.springframework.stereotype.*

/**
 * Encapsulates the machinery of interaction with Google's map API.
 */
@Component
class MapsIntegrationService(@Value("\${googlemaps.api.key}") private val googleMapsApiKey: String) {

    /**
     * Orders the users supplied by [findDrivers] function argument by travel time to the specified location,
     * returns at most [limit] closest users.
     */
    fun findNearestUsers(location: Location, findDrivers: () -> List<UserLocation>, limit: Int = 5): List<UserWithDistance> {
        val availableDrivers = findDrivers()
        if (availableDrivers.isEmpty()) return emptyList()

        val driverLocations = availableDrivers
            .map { it.coordinates.place.toString() }
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
            .take(limit)
            .map { UserWithDistance(
                it.second.userId,
                "${it.first.duration.humanReadable} (${it.first.distance.humanReadable})",
                DistanceInMeters(it.first.distance.inMeters),
                java.time.Duration.ofSeconds(it.first.duration.inSeconds)
            ) }
            .toList()
    }
}

data class HowFar(val distance: Distance, val duration: Duration)