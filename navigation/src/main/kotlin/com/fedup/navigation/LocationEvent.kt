package com.fedup.navigation

import com.fedup.shared.*

sealed class LocationEvent

/**
 * Sent by the user's mobile device when her position needs to be tracked in real time
 */
data class UserLocationChanged(val user: UserId, val position: Location): LocationEvent() {
    companion object
}

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
