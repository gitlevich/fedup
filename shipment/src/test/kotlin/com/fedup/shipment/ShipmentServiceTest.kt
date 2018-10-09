package com.fedup.shipment

import com.fedup.shared.*
import com.fedup.shared.protocol.location.*
import com.fedup.shipment.model.*
import com.nhaarman.mockito_kotlin.*
import org.junit.*
import java.time.*

class ShipmentServiceTest {
    private val shipmentRepository = mock<ShipmentRepository>()
    private val shippingService = ShippingService(shipmentRepository)
    private val shipper = Shipper("shipper@shippers.com")
    private val driver = Driver("driver@drivers.com")
    private val receiver = Receiver("receiver@receivers.com")
    private val pickupLocation = Location(37.7534327, -122.4344288)
    private val deliveryLocation = Location(37.7594658, -122.4349364)
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

        verify(shipmentRepository, times(1)).save(argForWhich { state == Shipment.State.PICKUP_REQUESTED })
    }

    @Test
    fun `when receiver issues acknowledgeUpcomingDelivery command, the Shipment transitions to UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER state`() {
        shippingService.acknowledgeUpcomingDelivery(shipment.trackingId, receiver, deliveryLocation)

        verify(shipmentRepository, times(1)).save(argForWhich { state == Shipment.State.UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER })
    }

    @Test
    fun `when driver issues acceptShipmentRequest command, the Shipment transitions to ASSIGNED_TO_DRIVER state`() {
        shippingService.acceptShipmentRequest(shipment.trackingId, driver, pickupLocation)

        verify(shipmentRepository, times(1)).save(argForWhich { state == Shipment.State.ASSIGNED_TO_DRIVER })
    }

    @Test
    fun `when driver issues reportPickup command, the Shipment transitions to PICKED_UP_AND_ON_THE_WAY state`() {
        val shipmentAssignedToDriver = shipment.assignedToDriver(driver, pickupLocation)
        given(shipmentRepository.findBy(shipment.trackingId)).willReturn(shipmentAssignedToDriver)

        shippingService.reportPickup(shipment.trackingId, shipper, driver, pickupLocation.toSTC())

        verify(shipmentRepository, times(1)).save(argForWhich { state == Shipment.State.PICKED_UP_AND_ON_THE_WAY })
    }

    @Test
    fun `when receiver issues confirmReceipt command, the Shipment transitions to DELIVERED state`() {
        val shipmentAssignedToDriver = shipment.assignedToDriver(driver, pickupLocation)
        given(shipmentRepository.findBy(shipment.trackingId)).willReturn(shipmentAssignedToDriver)

        shippingService.confirmReceipt(shipment.trackingId, driver, receiver, deliveryLocation.toSTC())

        verify(shipmentRepository, times(1)).save(argForWhich { state == Shipment.State.DELIVERED })
    }

    @Before
    fun setUp() {
        given(shipmentRepository.findBy(shipment.trackingId)).willReturn(shipment)
    }
}
