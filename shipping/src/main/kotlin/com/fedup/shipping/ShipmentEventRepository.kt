package com.fedup.shipping

import com.fedup.common.*
import org.springframework.stereotype.*

/**
 * Sits on top of event-sourced view of shipment requests by location and shipment stream
 */
@Component
class ShipmentEventRepository {

    fun save(shipmentEvent: ShipmentEvent) {
        TODO("not implemented")
    }

    fun historyOfPackageWith(trackingId: TrackingId): List<ShipmentEvent> {
        TODO("not implemented")
    }
}