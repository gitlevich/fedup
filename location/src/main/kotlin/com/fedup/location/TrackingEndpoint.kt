package com.fedup.location

import com.fedup.shared.protocol.location.*
import org.springframework.stereotype.*

// TODO make it a REST endpoint
@Component
class TrackingEndpoint(private val locationService: LocationService) {
    fun recordUserLocation(userId: String, location: Location) {
        locationService.recordUserLocation(UserLocation(userId, location))
    }

}