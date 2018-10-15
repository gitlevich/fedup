package com.fedup.location

import com.fedup.shared.protocol.location.*
import com.google.maps.*
import com.google.maps.model.*
import org.springframework.beans.factory.annotation.*
import org.springframework.stereotype.*

/**
 * Encapsulates the machinery of interaction with Google's map API.
 *
 * Needs a fallback mechanism (e.g. in case Google maps are inaccessible, our whole application becomes useless
 * as we can't locate drivers).
 */
@Component
class MapsIntegrationService(@Value("\${googlemaps.api.key}") private val googleMapsApiKey: String) {

    /**
     * Orders the users supplied by [userLocations] function argument by travel time to the specified location,
     * returns at most [limit] closest users.
     */
    fun findNearestUsers(location: Location, userLocations: List<UserLocation>, limit: Int = 5): List<UserWithDistance> {
        if (userLocations.isEmpty()) return emptyList()

        val driverLocations = userLocations
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
            .zip(userLocations)
            .asSequence()
            .sortedBy { it.first.duration.inSeconds }
            .take(limit)
            .map {
                UserWithDistance(
                    it.second.userId,
                    "${it.first.duration.humanReadable} (${it.first.distance.humanReadable})",
                    DistanceInMeters(it.first.distance.inMeters),
                    java.time.Duration.ofSeconds(it.first.duration.inSeconds)
                )
            }
            .toList()
    }
}

data class HowFar(val distance: Distance, val duration: Duration)
