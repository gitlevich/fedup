package com.fedup.location

import com.fedup.shared.protocol.location.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class MapsIntegrationServiceIT {
    @Autowired
    private lateinit var service: MapsIntegrationService
    private val shipperLocation = Location(37.7724868, -122.4166086)

    @Test
    fun `should find closest users ordered by time to travel`() {
        val nearestUsers = service.findNearestUsers(shipperLocation, fiveUsers)

        assertThat(nearestUsers)
            .hasSize(5)
            .anyMatch { it.userId == john.userId }
            .anyMatch { it.userId == jane.userId }
            .anyMatch { it.userId == vlad.userId }
            .anyMatch { it.userId == arnold.userId }
            .anyMatch { it.userId == elon.userId }
    }

    @Test
    fun `given the number of available users exceeds the limit, should throw away any extras`() {
        val nearestUsers = service.findNearestUsers(
            location = shipperLocation,
            userLocations = fiveUsers,
            limit = 2
        )

        assertThat(nearestUsers)
            .hasSize(2)
    }

    @Test
    fun `given the number of available users is lower than the limit, should return all available`() {
        val nearestUsers = service.findNearestUsers(
            location = shipperLocation,
            userLocations = fiveUsers,
            limit = 20
        )

        assertThat(nearestUsers)
            .hasSize(fiveUsers.size)
    }

    companion object {
        private val jane = UserLocation("jane@hotmail.com", STC(Location(37.7534327, -122.4344288)), UserRole.DRIVER)
        private val vlad = UserLocation("vlad@xaoc.com", STC(Location(37.726768, -122.390035)), UserRole.DRIVER)
        private val john = UserLocation("john@gmail.com", STC(Location(37.7124351, -122.3916016)), UserRole.DRIVER)
        private val arnold = UserLocation("arnold@governator.org", STC(Location(37.7824834, -121.4146101)), UserRole.DRIVER)
        private val elon = UserLocation("elon@tesla.com", STC(Location(38.4985740, -121.7593756)), UserRole.DRIVER)

        private val fiveUsers = listOf(jane, vlad, john, arnold, elon)
    }
}