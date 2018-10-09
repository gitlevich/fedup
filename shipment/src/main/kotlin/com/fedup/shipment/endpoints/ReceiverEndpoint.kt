package com.fedup.shipment.endpoints

import com.fedup.shared.*
import com.fedup.shared.protocol.location.*
import com.fedup.shipment.*
import com.fedup.shipment.model.*
import org.springframework.stereotype.*

// TODO expose as a REST endpoint
@Component
class ReceiverEndpoint(private val shippingService: ShippingService) {
    fun confirmPackageReceipt(trackingId: TrackingId, receiver: Receiver, at: SpaceTimeCoordinates) {
    }
}