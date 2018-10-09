package com.fedup.shipment

import com.fedup.shared.*
import com.fedup.shared.protocol.location.*
import com.fedup.shipment.model.*
import com.nhaarman.mockito_kotlin.*
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.time.*

@Ignore("Needs to be implemented")
@RunWith(SpringRunner::class)
@SpringBootTest
class ShipmentServiceIT {
    @Autowired private lateinit var shippingService: ShippingService
    private val shipper = Shipper("shipper@shippers.com")
    private val receiver = Receiver("receiver@receivers.com")
    private val pickupLocation = Location(37.7534327, -122.4344288)
    private val deliverBy = OffsetDateTime.parse("2018-10-11T17:00:00-00:08")
    private val shipment = Shipment(
        TrackingId.next(),
        RoutingSpec(shipper, receiver, deliverBy, pickupLocation),
        Shipment.State.PICKUP_REQUESTED,
        listOf(ShipmentHistoryRecord(Shipment.State.PICKUP_REQUESTED, SpaceTimeCoordinates(pickupLocation)))
    )

    @Test
    fun `when shipper issues requestShipmentPickup command, a new Shipment is created in PICKUP_REQUESTED state`() {
        shippingService.requestShipmentPickup(shipper, pickupLocation, receiver, deliverBy)

        TODO("Not implemented")
    }

    @Test
    fun `when receiver issues acknowledgeUpcomingDelivery command, the Shipment transitions to UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER state`() {
        TODO("Not implemented")
    }

    @Test
    fun `when receiver issues acknowledgeUpcomingDelivery command, NearbyDriversRequested event is pushed to driverRequests topic`() {
        TODO("Not implemented")
    }

    @Test
    fun `upon DriversLocated event, Notification event is pushed to notifications topic to inform drivers of shipment`() {
        TODO("Not implemented")
    }

    @Test
    fun `when driver issues acceptShipmentRequest command, the Shipment transitions to ASSIGNED_TO_DRIVER state`() {
        TODO("Not implemented")
    }

    @Test
    fun `when driver issues acceptShipmentRequest command, Notification event for shipper is pushed to notifications topic`() {
        TODO("Not implemented")
    }

    @Test
    fun `when driver issues reportPickup command, the Shipment transitions to PICKED_UP_AND_ON_THE_WAY state`() {
        TODO("Not implemented")
    }

    @Test
    fun `when driver issues reportPickup command, Notification event for receiver is pushed to notifications topic`() {
        TODO("Not implemented")
    }

    @Test
    fun `when receiver issues confirmReceipt command, the Shipment transitions to DELIVERED state`() {
        TODO("Not implemented")
    }
}
