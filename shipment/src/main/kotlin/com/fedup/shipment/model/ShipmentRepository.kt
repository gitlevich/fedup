package com.fedup.shipment.model

import com.fedup.shared.*
import com.fedup.shared.machinery.*
import org.springframework.stereotype.*

/**
 * Sits on top of a view of shipment requests by trackingId event-sourced from shipments stream
 */
@Component
class ShipmentRepository(private val streamsConfig: KafkaStreamsConfig) {

    fun save(shipment: Shipment) {
        TODO("not implemented")
    }

    fun findBy(trackingId: TrackingId): Shipment? {
        TODO("not implemented")
    }
}