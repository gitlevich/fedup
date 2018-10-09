package com.fedup.shared.protocol.location

import com.fedup.shared.*
import java.time.*

data class Location(val latitude: Double, val longitude: Double) {
    override fun toString(): String = "$latitude, $longitude"
}

fun Location.toSTC() = STC(this)
fun Location.toSTC(at: OffsetDateTime) = STC(this, at)

enum class UserRole { SHIPPER, DRIVER, RECEIVER }
data class UserLocation(val userId: UserId, val coordinates: STC, val userRole: UserRole) { companion object }

data class SpaceTimeCoordinates(val place: Location, val time: OffsetDateTime = OffsetDateTime.now())
typealias STC = SpaceTimeCoordinates

data class DistanceInMeters(val distance: Long) { companion object }
data class UserWithDistance(val userId: UserId, val distanceMessage: String, val distance: DistanceInMeters, val timeToTravel: Duration)

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
