package com.fedup.shipment.model

import com.fedup.shared.*
import com.fedup.shared.machinery.*
import org.springframework.stereotype.*

/**
 * Sits on top of event-sourced view of shipment requests by location and shipment stream
 */
@Component
class ShipmentRepository(private val streamsConfig: KafkaStreamsConfig) {

    fun save(shipment: Shipment) {
        TODO("not implemented")
    }

    fun historyOfPackageWith(trackingId: TrackingId): List<ShipmentHistoryRecord> {
        TODO("not implemented")
    }

    fun findBy(trackingId: TrackingId): Shipment? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}