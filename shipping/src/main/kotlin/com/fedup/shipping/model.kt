package com.fedup.shipping

import org.dddcommunity.*
import java.time.*
import java.util.*

data class UserId(val value: String)

sealed class User:  Entity<UserId>
data class Shipper(override val identity: UserId): User()
data class Receiver(override val identity: UserId): User()
data class Driver(override val identity: UserId): User()

data class Location(val latitude: Double, val longitude: Double)
data class RoutingSpecification(val origin: Location, val receiver: Receiver, val deliverBy: OffsetDateTime)

// We give directions to a driver to get from his current location to pickup location, then to drop-off location
data class Segment(val from: Location, val to: Location)
data class Directions(val segments: List<Segment>)

data class Leg(val driver: Driver, val from: Location, val to: Location)
data class Itinerary(val legs: List<Leg>)

data class TrackingId(val value: String) {
    companion object {
        fun next() = TrackingId(UUID.randomUUID().toString())
    }
}
data class Shipment(
    val trackingId: TrackingId,
    val routingSpecification: RoutingSpecification,
    val shipper: Shipper,
    val receiver: Receiver,
    val itinerary: Itinerary? = null
): Entity<TrackingId> {
    override val identity = trackingId
    fun withItinerary(itinerary: Itinerary) = copy(itinerary = itinerary)
}

data class SpaceTimeCoordinates(val location: Location, val time: OffsetDateTime)

data class UserLocationChanged(val user: User, val position: Location) // sent by the user when his position needs to be tracked in real time


sealed class ShipmentEvent {
    abstract val trackingId: TrackingId
}
data class ShipmentRequest(override val trackingId: TrackingId, val shipper: Shipper, val routingSpecification: RoutingSpecification): ShipmentEvent() // shipper to system, then system to driver
data class ShipmentRequestAccepted(override val trackingId: TrackingId, val driver: Driver): ShipmentEvent() // driver to system, then system to shipper
data class PickedUp(override val trackingId: TrackingId, val driver: Driver, val at: SpaceTimeCoordinates): ShipmentEvent()
data class HandedOff(override val trackingId: TrackingId, val acceptedBy: Driver, val at: SpaceTimeCoordinates): ShipmentEvent() // sent by acceptedBy driver
data class Delivered(override val trackingId: TrackingId, val receiver: Receiver, val at: SpaceTimeCoordinates): ShipmentEvent()



class ShipperService(private val shipmentEventRepository: ShipmentEventRepository) {
    fun requestShipment(from: Shipper, to: Receiver, at: Location, deliverBy: OffsetDateTime): TrackingId =
        TrackingId
            .next()
            .also { shipmentEventRepository.save(ShipmentRequest(it, from, RoutingSpecification(at, to, deliverBy))) }

    fun checkProgressFor(trackingId: TrackingId): List<ShipmentEvent> = shipmentEventRepository.historyOfPackageWith(trackingId)
}

class ReceiverService(private val shipmentEventRepository: ShipmentEventRepository) {
    fun confirmPackageReceipt(trackingId: TrackingId, receiver: Receiver, at: SpaceTimeCoordinates) {
        shipmentEventRepository.save(Delivered(trackingId, receiver, at))
    }
}

class DriverService(private val shipmentEventRepository: ShipmentEventRepository) {

    fun nextShipmentRequest(currentLocation: Location): ShipmentRequest? =
        shipmentEventRepository.findRequestsNear(currentLocation).firstOrNull()

    fun acceptShipmentRequest(request: ShipmentRequest, driver: Driver) {
        shipmentEventRepository.save(ShipmentRequestAccepted(request.trackingId, driver))
    }

    fun reportPickup(trackingId: TrackingId, driver: Driver, at: SpaceTimeCoordinates) {
        shipmentEventRepository.save(PickedUp(trackingId, driver, at))
    }

    fun reportHandOff(trackingId: TrackingId, acceptedBy: Driver, at: SpaceTimeCoordinates) {
        shipmentEventRepository.save(HandedOff(trackingId, acceptedBy, at))
    }
}

/**
 * Provides user location reporting capabilities. Writes to user-location stream
 */
class UserLocationService {
    fun reportLocation(user: User, location: Location) {
        TODO()
    }
}

/**
 * Sits on top of event-sourced view of shipment requests by location and shipment stream
 */
class ShipmentEventRepository {
    fun findRequestsNear(location: Location): List<ShipmentRequest> = TODO()

    fun save(shipmentEvent: ShipmentEvent) {
        TODO("not implemented")
    }

    fun historyOfPackageWith(trackingId: TrackingId): List<ShipmentEvent> {
        TODO("not implemented")
    }
}