package com.fedup.shipment.model

import com.fedup.shared.protocol.location.*
import com.fedup.shared.protocol.shipment.*
import com.fedup.shipment.model.Shipment.*
import com.fedup.shipment.model.Shipment.State.*
import org.assertj.core.api.Assertions.*
import org.junit.*
import java.time.*

class ShipmentTest {

    @Test
    fun `should be created in READY_FOR_PICKUP state`() {
        assertThat(Shipment.newShipmentWith(routingSpec).state).isEqualTo(READY_FOR_PICKUP)
    }

    @Test
    fun `should be created with a single shipment history record of type READY_FOR_PICKUP`() {
        assertThat(Shipment.newShipmentWith(routingSpec).history).hasSize(1).allMatch { it.type == READY_FOR_PICKUP }
    }

    @Test
    fun `should be created with no driver specified`() {
        assertThat(Shipment.newShipmentWith(routingSpec).driver).isNull()
    }

    @Test
    fun `should transition from READY_FOR_PICKUP to UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER on receiver acknowledgement`() {
        val shipment = makeShipmentInState(READY_FOR_PICKUP)

        assertThat(shipment.registerReceiverAcknowledgement(receiver, aNicePlace).state)
            .isEqualTo(UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER)
    }

    @Test
    fun `should transition from UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER to ASSIGNED_TO_DRIVER when a driver accepts it`() {
        val shipment = makeShipmentInState(UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER)

        assertThat(shipment.assignToDriver(driver, aNicePlace).state)
            .isEqualTo(ASSIGNED_TO_DRIVER)
    }

    @Test
    fun `should transition from ASSIGNED_TO_DRIVER to PICKED_UP_AND_ON_THE_WAY when a driver picks it up`() {
        val shipment = Shipment.newShipmentWith(routingSpec)
            .registerReceiverAcknowledgement(receiver, aNicePlace)
            .assignToDriver(driver, aNicePlace)
            .registerPickup(driver, shipper, aNicePlace)

        assertThat(shipment.state)
            .isEqualTo(PICKED_UP_AND_ON_THE_WAY)
    }

    @Test
    fun `should transition from PICKED_UP_AND_ON_THE_WAY to DELIVERED when a driver picks it up`() {
        val shipment = Shipment.newShipmentWith(routingSpec)
            .registerReceiverAcknowledgement(receiver, aNicePlace)
            .assignToDriver(driver, aNicePlace)
            .registerPickup(driver, shipper, aNicePlace)
            .registerDelivery(driver, receiver, aNicePlace.toSTC())

        assertThat(shipment.state)
            .isEqualTo(DELIVERED)
    }

    // Not being rigorous here yet, just spot-checking.
    @Test
    fun `should prevent illegal state transitions`() {
        assertThatExceptionOfType(IllegalStateTransitionRequested::class.java).isThrownBy {
            Shipment.newShipmentWith(routingSpec)
                .registerReceiverAcknowledgement(receiver, aNicePlace)
                .registerPickup(driver, shipper, aNicePlace)
        }

        assertThatExceptionOfType(IllegalStateTransitionRequested::class.java).isThrownBy {
            Shipment.newShipmentWith(routingSpec)
                .registerDelivery(driver, receiver, aNicePlace.toSTC())
                .registerPickup(driver, shipper, aNicePlace)
        }
    }

    @Test
    fun `should classify as legal transition from READY_FOR_PICKUP to UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER`() {
        assertThat(Shipment.isStateTransitionAllowed(READY_FOR_PICKUP, UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER))
            .isTrue()
    }

    @Test
    fun `should classify as legal transition from READY_FOR_PICKUP to EXCEPTION`() {
        assertThat(Shipment.isStateTransitionAllowed(READY_FOR_PICKUP, EXCEPTION))
            .isTrue()
    }

    @Test
    fun `should classify as illegal transition from UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER to READY_FOR_PICKUP`() {
        assertThat(Shipment.isStateTransitionAllowed(UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER, READY_FOR_PICKUP))
            .isFalse()
    }

    @Test
    fun `should have 3 history records after going through 3 state transitions`() {
        val shipment = Shipment.newShipmentWith(routingSpec)
            .registerReceiverAcknowledgement(receiver, aNicePlace)
            .assignToDriver(driver, aNicePlace)

        assertThat(shipment.history).hasSize(3)
        assertThat(shipment.history[0]).matches { it.type == READY_FOR_PICKUP }
        assertThat(shipment.history[1]).matches { it.type == UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER }
        assertThat(shipment.history[2]).matches { it.type == ASSIGNED_TO_DRIVER }
    }

    @Test
    fun `should have Entity-style equality behavior (only based on identity)`() {
        val trackingId = TrackingId.next()
        val shipmentReadyForPickup = makeShipmentInState(trackingId, READY_FOR_PICKUP)
        val deliveredShipment = makeShipmentInState(trackingId, DELIVERED)

        assertThat(deliveredShipment).isEqualTo(shipmentReadyForPickup)
    }

    @Test
    fun `should have Entity-style hashCode behavior (only based on identity)`() {
        val trackingId = TrackingId.next()
        val shipmentReadyForPickup = makeShipmentInState(trackingId, READY_FOR_PICKUP)
        val deliveredShipment = makeShipmentInState(trackingId, DELIVERED)

        assertThat(deliveredShipment.hashCode()).isEqualTo(shipmentReadyForPickup.hashCode())
    }

    companion object {
        private val aNicePlace = Location(37.755774, -122.419591)
        private val driver = Driver("driver@drivers.com")
        private val receiver = Receiver("receiver@receivers.com")
        private val shipper = Shipper("shipper@shippers.com")

        private val routingSpec = RoutingSpec(
            shipper = shipper,
            receiver = receiver,
            deliverBy = OffsetDateTime.parse("2018-10-11T17:00:00-00:08"),
            originalPickupLocation = aNicePlace
        )

        private fun makeShipmentInState(state: State): Shipment =
            makeShipmentInState(TrackingId.next(), state)

        private fun makeShipmentInState(trackingId: TrackingId, state: State): Shipment =
            Shipment(
                trackingId,
                RoutingSpec(
                    shipper,
                    Receiver("receiver@receivers.com"),
                    OffsetDateTime.parse("2018-10-11T17:00:00-00:08"),
                    Location(37.7534327, -122.4344288)
                ),
                state,
                listOf()
            )
    }
}