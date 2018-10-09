package com.fedup.location

import com.fedup.shared.protocol.location.*
import com.nhaarman.mockito_kotlin.*
import org.junit.*
import org.junit.runner.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.autoconfigure.web.servlet.*
import org.springframework.boot.test.context.*
import org.springframework.boot.test.mock.mockito.*
import org.springframework.http.*
import org.springframework.test.context.junit4.*
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.request.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.lang.RuntimeException
import java.time.*


@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class TrackingEndpointTest {
    @Autowired private lateinit var mvc: MockMvc
    @MockBean private lateinit var locationService: LocationService

    private val userLocation = UserLocation(
        "driver@drivers.com",
        STC(Location(37.7724868, 122.4166086), OffsetDateTime.parse("2018-10-11T17:00:00-00:08"))
    )

    @Test
    fun `given location is reported by existing user, 200 status is expected`() {
        mvc.perform(MockMvcRequestBuilders.post("/location/user")
            .content(userLocation.asJson())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
    }

    @Test
    fun `given location is reported by unknown user, 404 status is expected`() {
        doAnswer { throw UserNotFound("42") }
            .whenever(locationService)
            .recordUserLocation(any())

        mvc.perform(MockMvcRequestBuilders.post("/location/user")
            .content(userLocation.asJson())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `given no drivers are available, 404 status is expected`() {
        doAnswer { throw NoDriversAvailable(RuntimeException("because something broke")) }
            .whenever(locationService)
            .recordUserLocation(any())

        mvc.perform(MockMvcRequestBuilders.post("/location/user")
            .content(userLocation.asJson())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }
}