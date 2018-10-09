package com.fedup.shipment.model

import com.fedup.shared.*
import com.fedup.shared.Characterization.Entity
import com.fedup.shared.protocol.location.*
import java.time.*

data class Shipment internal constructor(
    val trackingId: TrackingId,
    val routingSpec: RoutingSpec,
    val state: State,
    val history: List<ShipmentHistoryRecord>,
    val driver: Driver? = null
) : Entity<TrackingId>() {
    override val identity = trackingId

    fun assignedToDriver(driver: Driver, pickupLocation: Location) =
        copy(driver = driver).transitionedTo(State.ASSIGNED_TO_DRIVER, pickupLocation.toSTC())

    fun acknowledgedByReceiver(receiver: Receiver, deliveryLocation: Location) =
        copy(routingSpec = routingSpec.copy(deliveryLocation = deliveryLocation))
            .transitionedTo(State.UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER, deliveryLocation.toSTC())

    fun pickedUp(driver: Driver, shipper: Shipper, at: SpaceTimeCoordinates) =
        when {
            this.driver != driver          -> throw ShipmentException("Expected ${this.driver}, actual $driver")
            routingSpec.shipper != shipper -> throw ShipmentException("Expected ${routingSpec.shipper}, actual $shipper")
            else                           -> copy(driver = driver).transitionedTo(State.PICKED_UP_AND_ON_THE_WAY, at)
        }

    fun delivered(driver: Driver, receiver: Receiver, at: SpaceTimeCoordinates) =
        when {
            this.driver != driver            -> throw ShipmentException("Expected ${this.driver}, actual $driver")
            routingSpec.receiver != receiver -> throw ShipmentException("Expected ${routingSpec.receiver}, actual $receiver")
            else                             -> copy(driver = driver).transitionedTo(State.DELIVERED, at)
        }

    private fun transitionedTo(state: State, at: SpaceTimeCoordinates) =
        copy(state = state).copy(history = history + ShipmentHistoryRecord(state, at))

    enum class State {
        PICKUP_REQUESTED,
        UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER,
        ASSIGNED_TO_DRIVER,
        PICKED_UP_AND_ON_THE_WAY,
        DELIVERED
    }

    companion object {
        fun newShipmentWith(routingSpec: RoutingSpec): Shipment =
            Shipment(
                trackingId = TrackingId.next(),
                routingSpec = routingSpec,
                state = State.PICKUP_REQUESTED,
                history = listOf(ShipmentHistoryRecord(State.PICKUP_REQUESTED, SpaceTimeCoordinates(routingSpec.originalPickupLocation)))
            )
    }
}

class ShipmentException(message: String) : Exception(message)

data class RoutingSpec(
    val shipper: Shipper,
    val receiver: Receiver,
    val deliverBy: OffsetDateTime,
    val originalPickupLocation: Location,
    val deliveryLocation: Location? = null
) {
    fun withDeliveryLocation(location: Location) = copy(deliveryLocation = location)
}

data class ShipmentHistoryRecord(val type: Shipment.State, val at: SpaceTimeCoordinates)



fun Shipment.Companion.fromBytes(bytes: ByteArray): Shipment = objectMapper.readValue(bytes, Shipment::class.java)
fun Shipment.Companion.fromJson(string: String): Shipment = objectMapper.readValue(string, Shipment::class.java)
fun Shipment.asBytes(): ByteArray = objectMapper.writeValueAsBytes(this)
fun Shipment.asJson(): String = objectMapper.writeValueAsString(this)
