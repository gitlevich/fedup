package com.fedup.shipping

import com.fedup.common.*
import org.springframework.stereotype.*
import java.time.*

// TODO expose as a REST endpoint
@Component
class ShipperEndpoint(
    private val shippingCommands: ShippingCommands,
    private val shipmentEventRepository: ShipmentEventRepository
) {

    /**
     * This method is the command by the shipper for someone to come pick up her shipment and deliver it
     * to the receiver at
     */
    fun requestShipmentPickup(from: Shipper, at: Location, to: Receiver, deliverBy: OffsetDateTime) {
    }

    fun checkProgressFor(trackingId: TrackingId): List<ShipmentEvent> = shipmentEventRepository.historyOfPackageWith(trackingId)
}