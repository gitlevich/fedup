package com.fedup.shipping

import com.fedup.common.*
import org.springframework.stereotype.*

// TODO expose as a REST endpoint
@Component
class DriverEndpoint(private val shippingCommands: ShippingCommands) {

    fun acceptShipmentRequest(request: ShipmentRequested, driver: Driver) {
    }

    fun reportPickup(trackingId: TrackingId, driver: Driver, at: SpaceTimeCoordinates) {
    }

    fun reportHandOff(trackingId: TrackingId, acceptedBy: Driver, at: SpaceTimeCoordinates) {
    }
}