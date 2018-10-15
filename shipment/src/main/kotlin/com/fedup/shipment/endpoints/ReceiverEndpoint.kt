package com.fedup.shipment.endpoints

import com.fedup.shared.protocol.location.*
import com.fedup.shared.protocol.shipment.*
import com.fedup.shipment.*
import com.fedup.shipment.model.*
import org.springframework.stereotype.*

// TODO expose as a REST endpoint
@Component
class ReceiverEndpoint(private val shippingService: ShippingService) {

    fun acknowledgeUpcomingDelivery(trackingId: TrackingId, receiver: Receiver, at: SpaceTimeCoordinates) {
    }

    fun confirmPackageReceipt(trackingId: TrackingId, receiver: Receiver, at: SpaceTimeCoordinates) {
    }
}