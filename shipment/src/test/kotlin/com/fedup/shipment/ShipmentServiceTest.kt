package com.fedup.shipment

import com.fedup.shipment.model.*
import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class ShipmentServiceTest {
    @Autowired private lateinit var shippingService: ShippingService
    private val shipmentRepository = mock<ShipmentRepository>()

    @Test
    fun contextLoads() {
    }

}
