package com.fedup.shipment.endpoints

import com.fedup.common.*
import com.fedup.shipment.*
import com.fedup.shipment.model.*
import org.springframework.stereotype.*

// TODO expose as a REST endpoint
@Component
class DriverEndpoint(private val shipmentFacade: ShipmentFacade) {
    init {
        println("DriverEndpoint Running")
    }

    fun acceptShipmentRequest(trackingId: TrackingId, driver: Driver) {
    }

    fun reportPickup(trackingId: TrackingId, driver: Driver, at: SpaceTimeCoordinates) {
    }

    fun reportHandOff(trackingId: TrackingId, acceptedBy: Driver, at: SpaceTimeCoordinates) {
    }
}