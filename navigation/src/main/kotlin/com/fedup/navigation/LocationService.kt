package com.fedup.navigation

import com.fedup.common.*
import com.google.maps.*
import com.google.maps.model.*

data class UserLocation(val userId: UserId, val location: Location)
data class HowFar(val distance: Distance, val duration: Duration)
data class UserWithDistance(val userId: UserId, val distanceMessage: String)

data class UserLocationChanged(val user: UserId, val position: Location) // sent by the user when his position needs to be tracked in real time


class LocationService {
    fun reportUserLocation(userId: String, location: Location) {
        // publishes user location to the user-location stream
    }

    fun closestDrivers(location: Location): List<UserWithDistance> {
        val availableDrivers = findAvailableDrivers()
        val driverLocations = availableDrivers
            .map { it.location.toString() }
            .toTypedArray()

        val context = GeoApiContext.Builder()
            .apiKey("AIzaSyBKFAiP9PjPaB8V-XtMivC1gc4HMLuiK8M")
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



    private fun findAvailableDrivers(): List<UserLocation> {
        return listOf(UserLocation("1", Location(37.7724868,-122.4166086)), UserLocation("2", Location(37.7339012,-122.4194585)), UserLocation("3", Location(37.7486923,-122.4186179)))
    }
}

fun main(args: Array<String>) {
    val closestDrivers = LocationService().closestDrivers(Location(37.755774, -122.419591))
    closestDrivers.forEach { println(it) }
}