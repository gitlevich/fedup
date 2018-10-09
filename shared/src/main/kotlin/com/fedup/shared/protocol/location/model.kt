package com.fedup.shared.protocol.location

import com.fedup.shared.*
import java.time.*

data class Location(val latitude: Double, val longitude: Double) {
    override fun toString(): String = "$latitude, $longitude"
}
data class UserLocation(val userId: UserId, val location: STC) { companion object }

data class SpaceTimeCoordinates(val location: Location, val time: OffsetDateTime = OffsetDateTime.now())
typealias STC = SpaceTimeCoordinates
data class UserWithDistance(val userId: UserId, val distanceMessage: String)

sealed class LocationEvent

/**
 * Requests drivers close to the specified location to pick up a shipment with the specified [TrackingId]
 */
data class NearbyDriversRequested(val trackingId: TrackingId, val location: Location, val howMany: Int = 5): LocationEvent() {
    companion object
}

/**
 * Published to shipment stream, where it's picked up by shipping service and translated to a series of notification
 * events for each driver
 */
data class DriversLocated(val trackingId: TrackingId, val drivers: List<UserWithDistance>): LocationEvent() {
    companion object
}
