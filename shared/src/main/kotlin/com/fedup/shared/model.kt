package com.fedup.shared

import java.time.*

typealias UserId = String

data class Location(val latitude: Double, val longitude: Double) {
    override fun toString(): String = "$latitude, $longitude"
}
data class UserLocation(val userId: UserId, val location: Location)

data class SpaceTimeCoordinates(val location: Location, val time: OffsetDateTime = OffsetDateTime.now())
typealias STC = SpaceTimeCoordinates

