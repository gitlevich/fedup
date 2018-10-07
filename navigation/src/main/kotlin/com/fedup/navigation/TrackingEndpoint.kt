package com.fedup.navigation

import com.fedup.common.*
import org.springframework.stereotype.*

// TODO make it a REST endpoint
@Component
class TrackingEndpoint(private val locationService: LocationService) {
    fun recordUserLocation(userId: String, location: Location) {
        locationService.recordUserLocation(UserLocation(userId, location))
    }

}