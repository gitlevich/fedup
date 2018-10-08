package com.fedup.shipment.endpoints

import com.fedup.shared.*
import com.fedup.shipment.*
import com.fedup.shipment.model.*
import org.springframework.stereotype.*
import java.time.*

// TODO expose as a REST endpoint
@Component
class ShipperEndpoint(
    private val shipmentFacade: ShipmentFacade,
    private val shipmentRepository: ShipmentRepository
) {

    /**
     * This method is the command by the shipper for someone to come pick up her shipment and deliver it
     * to the receiver at
     */
    fun requestShipmentPickup(from: Shipper, at: Location, to: Receiver, deliverBy: OffsetDateTime) {
    }

    fun checkProgressFor(trackingId: TrackingId): List<ShipmentHistoryRecord> = shipmentRepository.historyOfPackageWith(trackingId)
}