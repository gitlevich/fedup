package com.fedup.shipment.model

import com.fedup.shared.*
import com.fedup.shared.Characterization.Entity
import com.fedup.shared.protocol.location.*
import com.fedup.shipment.model.Shipment.State.*
import java.time.*

/**
 * A Shipment plays the central role in Shipment Service. It is the root of the its own aggregate.
 *
 * Throughout its life, it transitions through several states, from READY_FOR_PICKUP to DELIVERED.
 * Each transition is done via one of the methods that can be interpreted as commands.
 *
 * Shipment enforces its invariants, such as having a driver after it transitioned to ASSIGNED_TO_DRIVER
 * state. It should also enforce its state transitions (e.g. by consulting with its state transition graph
 * to see if requested transition is legal).
 */
data class Shipment internal constructor(
    val trackingId: TrackingId,
    val routingSpec: RoutingSpec,
    val state: State,
    val history: List<ShipmentHistoryRecord>,
    val driver: Driver? = null
) : Entity<TrackingId>() {
    override val identity = trackingId

    fun assignToDriver(driver: Driver, pickupLocation: Location) =
        copy(driver = driver).transitionedTo(ASSIGNED_TO_DRIVER, pickupLocation.toSTC())

    fun registerReceiverAcknowledgement(receiver: Receiver, deliveryLocation: Location) =
        when (receiver) {
            routingSpec.receiver -> copy(routingSpec = routingSpec.withDeliveryLocation(deliveryLocation))
                .transitionedTo(UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER, deliveryLocation.toSTC())
            else                 -> throw ShipmentException("Acknowledged by unexpected receiver $receiver, expected ${routingSpec.receiver}")
        }

    fun registerPickup(driver: Driver, shipper: Shipper, pickupLocation: Location) =
        when {
            isUnexpectedDriver(driver)    -> throw ShipmentException("Expected ${this.driver}, actual $driver")
            routingSpec.shipper != shipper -> throw ShipmentException("Expected ${routingSpec.shipper}, actual $shipper")
            else                           -> copy(driver = driver).transitionedTo(PICKED_UP_AND_ON_THE_WAY, pickupLocation.toSTC())
        }

    private fun isUnexpectedDriver(driver: Driver) = this.driver != null && this.driver != driver

    fun registerDelivery(driver: Driver, receiver: Receiver, at: SpaceTimeCoordinates) =
        when {
            isUnexpectedDriver(driver)      -> throw ShipmentException("Expected ${this.driver}, actual $driver")
            routingSpec.receiver != receiver -> throw ShipmentException("Expected ${routingSpec.receiver}, actual $receiver")
            else                             -> copy(driver = driver).transitionedTo(DELIVERED, at)
        }

    private fun transitionedTo(newState: State, at: SpaceTimeCoordinates) =
        if (isStateTransitionAllowed(state, newState))
            copy(state = newState).copy(history = history + ShipmentHistoryRecord(newState, at))
        else
            throw IllegalStateTransitionRequested(state, newState)

    enum class State {
        READY_FOR_PICKUP,
        UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER,
        ASSIGNED_TO_DRIVER,
        PICKED_UP_AND_ON_THE_WAY,
        DELIVERED,
        EXCEPTION
    }

    companion object {
        private val allowedStateTransitions = mapOf(
            READY_FOR_PICKUP to setOf(UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER, EXCEPTION),
            UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER to setOf(ASSIGNED_TO_DRIVER, EXCEPTION),
            ASSIGNED_TO_DRIVER to setOf(PICKED_UP_AND_ON_THE_WAY, EXCEPTION),
            PICKED_UP_AND_ON_THE_WAY to setOf(DELIVERED, EXCEPTION)
        )

        fun newShipmentWith(routingSpec: RoutingSpec): Shipment =
            Shipment(
                trackingId = TrackingId.next(),
                routingSpec = routingSpec,
                state = READY_FOR_PICKUP,
                history = listOf(ShipmentHistoryRecord(READY_FOR_PICKUP, SpaceTimeCoordinates(routingSpec.originalPickupLocation)))
            )

        fun isStateTransitionAllowed(currentState: State, nextState: State) =
            allowedStateTransitions[currentState]?.contains(nextState) ?: false
    }
}

open class ShipmentException(message: String) : Exception(message)
class IllegalStateTransitionRequested(from: Shipment.State, to: Shipment.State) :
    ShipmentException("Illegal state transition requested: $from->$to")

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
fun Shipment.asBytes(): ByteArray = objectMapper.writeValueAsBytes(this)
