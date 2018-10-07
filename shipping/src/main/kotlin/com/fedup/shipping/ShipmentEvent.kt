package com.fedup.shipping

import com.fedup.common.*
import java.time.*

sealed class ShipmentEvent {
    abstract val trackingId: TrackingId
}

/**
 * The shipper requested a pickup for her shipment.
 */
data class ShipmentRequested(
    override val trackingId: TrackingId,
    val shipper: UserLocation,
    val receiver: Receiver,
    val deliverBy: OffsetDateTime
): ShipmentEvent()

/**
 * A driver accepted the request
 */
data class ShipmentRequestAccepted(override val trackingId: TrackingId, val driver: Driver): ShipmentEvent()

/**
 * Driver has picked up the shipment from the shipper
 */
data class PickedUp(override val trackingId: TrackingId, val driver: Driver, val at: SpaceTimeCoordinates): ShipmentEvent()

/**
 * Driver has handed the shipment off to another driver to deliver it further
 */
data class HandedOff(override val trackingId: TrackingId, val acceptedBy: Driver, val at: SpaceTimeCoordinates): ShipmentEvent()

/**
 * The receiver has acknowledged the receipt of the shipment
 */
data class Delivered(override val trackingId: TrackingId, val receiver: Receiver, val at: SpaceTimeCoordinates): ShipmentEvent()
