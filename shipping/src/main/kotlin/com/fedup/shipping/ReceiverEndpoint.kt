package com.fedup.shipping

import com.fedup.common.*
import org.springframework.stereotype.*

// TODO expose as a REST endpoint
@Component
class ReceiverEndpoint(private val shippingCommands: ShippingCommands) {
    fun confirmPackageReceipt(trackingId: TrackingId, receiver: Receiver, at: SpaceTimeCoordinates) {
    }
}