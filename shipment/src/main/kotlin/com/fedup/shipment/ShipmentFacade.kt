package com.fedup.shipment

import com.fedup.shared.*
import com.fedup.shared.protocol.location.*
import com.fedup.shipment.model.*
import org.springframework.stereotype.*
import java.time.*

@Component
class ShipmentFacade(private val shipmentRepository: ShipmentRepository) {

    fun requestShipmentPickup(from: Shipper, at: Location, to: Receiver, deliverBy: OffsetDateTime) {
        shipmentRepository.save(
            Shipment.newShipmentWith(
                RoutingSpec(
                    shipper = from,
                    receiver = to,
                    deliverBy = deliverBy,
                    originalPickupLocation = at
                )
            )
        )
    }

    fun checkProgressFor(trackingId: TrackingId): List<ShipmentHistoryRecord> = shipmentRepository.historyOfPackageWith(trackingId)

    fun acceptShipmentRequest(trackingId: TrackingId, driver: Driver, location: Location) {
        val shipment = shipmentRepository.findBy(trackingId)
        shipment?.let { shipmentRepository.save(shipment.assignedToDriver(driver, at = SpaceTimeCoordinates(location))) }
            ?: UnknownShipmentException(trackingId)
    }

    fun reportPickup(trackingId: TrackingId, shipper: Shipper, driver: Driver, at: SpaceTimeCoordinates) {
        val shipment = shipmentRepository.findBy(trackingId)
        shipment?.let { shipmentRepository.save(shipment.pickedUp(driver, shipper, at)) }
            ?: UnknownShipmentException(trackingId)
    }

    fun confirmReceipt(trackingId: TrackingId, driver: Driver, receiver: Receiver, at: SpaceTimeCoordinates) {
        val shipment = shipmentRepository.findBy(trackingId)
        shipment?.let { shipmentRepository.save(shipment.delivered(driver, receiver, at)) }
            ?: UnknownShipmentException(trackingId)
    }

}

class UnknownShipmentException(trackingId: TrackingId) : Exception("Unknown tracking id $trackingId")