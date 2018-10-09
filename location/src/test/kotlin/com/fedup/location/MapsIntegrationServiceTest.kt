package com.fedup.location

import com.fedup.shared.protocol.location.*
import org.assertj.core.api.Assertions.*
import org.junit.*
import org.junit.runner.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.context.*
import org.springframework.test.context.junit4.*
import java.time.*

@RunWith(SpringRunner::class)
@SpringBootTest
class MapsIntegrationServiceTest {
    @Autowired private lateinit var service: MapsIntegrationService
    private val shipperLocation =  Location(37.7724868, -122.4166086)

    @Test
    fun `should find closest users ordered by time to travel`() {
        val nearestUsers = service.findNearestUsers(shipperLocation, { fiveUsers })

        assertThat(nearestUsers)
            .hasSize(5)
            .containsExactly(
                UserWithDistance(john.userId, "11 mins (9.4 km)", DistanceInMeters(9417), Duration.ofSeconds(676)),
                UserWithDistance(jane.userId, "13 mins (4.0 km)", DistanceInMeters(3993), Duration.ofSeconds(777)),
                UserWithDistance(vlad.userId, "14 mins (9.4 km)", DistanceInMeters(9404), Duration.ofSeconds(819)),
                UserWithDistance(arnold.userId, "1 hour 6 mins (104 km)", DistanceInMeters(103602), Duration.ofSeconds(3966)),
                UserWithDistance(elon.userId, "1 hour 12 mins (113 km)", DistanceInMeters(112851), Duration.ofSeconds(4314))
            )
    }

    @Test
    fun `given the number of available users exceeds the limit, should throw away any extras`() {
        val nearestUsers = service.findNearestUsers(
            location = shipperLocation,
            findDrivers = { fiveUsers },
            limit = 2
        )

        assertThat(nearestUsers)
            .hasSize(2)
    }

    @Test
    fun `given the number of available users is lower than the limit, should return all available`() {
        val nearestUsers = service.findNearestUsers(
            location = shipperLocation,
            findDrivers = { fiveUsers },
            limit = 20
        )

        assertThat(nearestUsers)
            .hasSize(fiveUsers.size)
    }

    companion object {
        private val jane = UserLocation("jane@hotmail.com", STC(Location(37.7534327, -122.4344288)))
        private val vlad = UserLocation("vlad@xaoc.com", STC(Location(37.726768, -122.390035)))
        private val john = UserLocation("john@gmail.com", STC(Location(37.7124351, -122.3916016)))
        private val arnold = UserLocation("arnold@governator.org", STC(Location(37.7824834, -121.4146101)))
        private val elon = UserLocation("elon@tesla.com", STC(Location(38.4985740, -121.7593756)))

        private val fiveUsers = listOf(jane, vlad, john, arnold, elon)
    }
}