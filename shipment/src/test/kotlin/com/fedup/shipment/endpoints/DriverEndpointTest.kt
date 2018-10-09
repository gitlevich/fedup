package com.fedup.shipment.endpoints

import com.fedup.shared.*
import com.fedup.shared.protocol.location.*
import com.fedup.shipment.*
import com.fedup.shipment.model.*
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
import org.springframework.test.web.servlet.result.*
import java.lang.RuntimeException

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class DriverEndpointTest {
    @Autowired private lateinit var mvc: MockMvc
    @MockBean private lateinit var shippingService: ShippingService
    private val trackingId = TrackingId("123-456-789")
    private val driver = Driver("driver@drivers.com")
    private val location = Location(37.7724868, 122.4166086)

    @Test
    fun `given shipment request is accepted by an existing driver, 200 code is expected`() {
        mvc.perform(MockMvcRequestBuilders.post("/shipment/${trackingId.value}/${driver.identity}")
            .content(location.asJson())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `given shipment request is accepted for an unknown tracking number, 404 code is expected`() {
        doAnswer { throw UnknownShipmentException(trackingId) }
            .whenever(shippingService)
            .acceptShipmentRequest(trackingId, driver, location)

        mvc.perform(MockMvcRequestBuilders.post("/shipment/${trackingId.value}/${driver.identity}")
            .content(location.asJson())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `given request fails with any other issue, 500 code is expected`() {
        doAnswer { throw RuntimeException() }
            .whenever(shippingService)
            .acceptShipmentRequest(trackingId, driver, location)

        mvc.perform(MockMvcRequestBuilders.post("/shipment/${trackingId.value}/${driver.identity}")
            .content(location.asJson())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError)
    }
}