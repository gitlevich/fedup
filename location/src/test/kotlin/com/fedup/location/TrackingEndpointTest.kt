package com.fedup.location

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


@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class TrackingEndpointTest {
    @Autowired private lateinit var mvc: MockMvc
    @MockBean private lateinit var locationService: LocationService

    @Test
    fun `given location is reported by existing user, 200 response is expected`() {
        mvc.perform(MockMvcRequestBuilders.post("/location/user")
            .content("""{"userId":"1","location":{"latitude":37.7724868,"longitude":-122.4166086}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
    }

    @Test
    fun `given location is reported by unknown user, not found response is expected`() {
        doAnswer { throw UserNotFoundException("42") }
            .whenever(locationService)
            .recordUserLocation(any())

        mvc.perform(MockMvcRequestBuilders.post("/location/user")
            .content("""{"userId":"1","location":{"latitude":37.7724868,"longitude":-122.4166086}}""")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

}