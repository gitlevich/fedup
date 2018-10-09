package com.fedup.shipment.model

import com.fedup.shared.*
import com.fedup.shared.protocol.location.*
import com.fedup.shipment.model.Shipment.*
import org.assertj.core.api.Assertions.*
import org.junit.*
import java.time.*

class ShipmentTest {
    private val aNicePlace = Location(37.755774, -122.419591)
    private val routingSpec = RoutingSpec(
        shipper = Shipper("shipper@shippers.com"),
        receiver = Receiver("receiver@receivers.com"),
        deliverBy = OffsetDateTime.parse("2018-10-11T17:00:00-00:08"),
        originalPickupLocation = aNicePlace
    )

    @Test
    fun `should be created in PICKUP_REQUESTED state`() {
        assertThat(Shipment.newShipmentWith(routingSpec).state).isEqualTo(State.PICKUP_REQUESTED)
    }

    @Test
    fun `should be created with a single shipment history record of type PICKUP_REQUESTED`() {
        assertThat(Shipment.newShipmentWith(routingSpec).history).hasSize(1).allMatch { it.type == State.PICKUP_REQUESTED }
    }

    @Test
    fun `should transition to ASSIGNED_TO_DRIVER when a driver accepts it`() {
        val shipment = Shipment.newShipmentWith(routingSpec).assignedToDriver(Driver("john@drivers.com"), aNicePlace)
        assertThat(shipment.state).isEqualTo(State.ASSIGNED_TO_DRIVER)
    }

    @Test
    fun `should have two history records after getting assigned to driver`() {
        val shipment = Shipment.newShipmentWith(routingSpec).assignedToDriver(Driver("john@drivers.com"), aNicePlace)
        assertThat(shipment.history).hasSize(2)
        assertThat(shipment.history[0]).matches { it.type == State.PICKUP_REQUESTED }
        assertThat(shipment.history[1]).matches { it.type == State.ASSIGNED_TO_DRIVER }
    }

    @Test
    fun `should have Entity-style equality behavior (only based on identity)`() {
        val pickupLocation = Location(37.7534327, -122.4344288)
        val trackingId = TrackingId.next()
        val shipmentReadyForPickup = Shipment(
            trackingId,
            RoutingSpec(Shipper("shipper@shippers.com"), Receiver("receiver@receivers.com"), OffsetDateTime.parse("2018-10-11T17:00:00-00:08"), pickupLocation),
            Shipment.State.PICKUP_REQUESTED,
            listOf(ShipmentHistoryRecord(Shipment.State.PICKUP_REQUESTED, SpaceTimeCoordinates(pickupLocation)))
        )
        val deliveredShipment = Shipment(
            trackingId,
            RoutingSpec(Shipper("shipper@shippers.com"), Receiver("receiver@receivers.com"), OffsetDateTime.parse("2018-10-11T17:00:00-00:08"), pickupLocation),
            Shipment.State.DELIVERED,
            listOf(ShipmentHistoryRecord(Shipment.State.PICKUP_REQUESTED, SpaceTimeCoordinates(pickupLocation)))
        )

        assertThat(deliveredShipment).isEqualTo(shipmentReadyForPickup)
    }

    @Test
    fun `should have Entity-style hashCode behavior (only based on identity)`() {
        val pickupLocation = Location(37.7534327, -122.4344288)
        val trackingId = TrackingId.next()
        val shipmentReadyForPickup = Shipment(
            trackingId,
            RoutingSpec(Shipper("shipper@shippers.com"), Receiver("receiver@receivers.com"), OffsetDateTime.parse("2018-10-11T17:00:00-00:08"), pickupLocation),
            Shipment.State.PICKUP_REQUESTED,
            listOf(ShipmentHistoryRecord(Shipment.State.PICKUP_REQUESTED, SpaceTimeCoordinates(pickupLocation)))
        )
        val deliveredShipment = Shipment(
            trackingId,
            RoutingSpec(Shipper("shipper@shippers.com"), Receiver("receiver@receivers.com"), OffsetDateTime.parse("2018-10-11T17:00:00-00:08"), pickupLocation),
            Shipment.State.DELIVERED,
            listOf(ShipmentHistoryRecord(Shipment.State.PICKUP_REQUESTED, SpaceTimeCoordinates(pickupLocation)))
        )

        assertThat(deliveredShipment.hashCode()).isEqualTo(shipmentReadyForPickup.hashCode())
    }
}