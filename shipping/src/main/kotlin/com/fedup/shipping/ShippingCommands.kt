package com.fedup.shipping

import com.fedup.common.*
import org.springframework.stereotype.*
import java.time.*

@Component
class ShippingCommands(private val shipmentEventRepository: ShipmentEventRepository) {
    /**
     * This method is the command by the shipper for someone to come pick up her shipment and deliver it
     * to the receiver at
     */
    fun requestShipmentPickup(from: Shipper, at: Location, to: Receiver, deliverBy: OffsetDateTime) {
        shipmentEventRepository.save(
            ShipmentRequested(
                trackingId = TrackingId.next(),
                shipper = UserLocation(to.identity, at),
                receiver = to,
                deliverBy = deliverBy
            )
        )
    }

    fun checkProgressFor(trackingId: TrackingId): List<ShipmentEvent> = shipmentEventRepository.historyOfPackageWith(trackingId)

    fun acceptShipmentRequest(request: ShipmentRequested, driver: Driver) {
        shipmentEventRepository.save(ShipmentRequestAccepted(request.trackingId, driver))
    }

    fun reportPickup(trackingId: TrackingId, driver: Driver, at: SpaceTimeCoordinates) {
        shipmentEventRepository.save(PickedUp(trackingId, driver, at))
    }

    fun reportHandOff(trackingId: TrackingId, acceptedBy: Driver, at: SpaceTimeCoordinates) {
        shipmentEventRepository.save(HandedOff(trackingId, acceptedBy, at))
    }

    fun confirmPackageReceipt(trackingId: TrackingId, receiver: Receiver, at: SpaceTimeCoordinates) {
        shipmentEventRepository.save(Delivered(trackingId, receiver, at))
    }

}