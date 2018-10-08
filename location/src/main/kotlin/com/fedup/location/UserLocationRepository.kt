package com.fedup.location

import com.fedup.shared.*
import com.fedup.shared.protocol.location.*
import org.springframework.stereotype.*

/**
 * Sits on top of user-location stream-backed KTable
 */
@Component
class UserLocationRepository {
    fun findAvailableDrivers(): List<UserLocation> {
        return listOf(
            UserLocation("1", Location(37.7724868, -122.4166086)),
            UserLocation("2", Location(37.7339012, -122.4194585)),
            UserLocation("3", Location(37.7486923, -122.4186179))
        )
    }

    fun saveUserLocation(userLocation: UserLocation) {
        println("USER LOCATION SAVED: $userLocation")
    }
}

class UserNotFoundException(userId: UserId): Exception("User $userId not found")