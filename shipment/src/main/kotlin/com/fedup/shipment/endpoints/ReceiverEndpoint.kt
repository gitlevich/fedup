package com.fedup.shipment.endpoints

import com.fedup.common.*
import com.fedup.shipment.*
import com.fedup.shipment.model.*
import org.springframework.stereotype.*

// TODO expose as a REST endpoint
@Component
class ReceiverEndpoint(private val shipmentFacade: ShipmentFacade) {
    fun confirmPackageReceipt(trackingId: TrackingId, receiver: Receiver, at: SpaceTimeCoordinates) {
    }
}