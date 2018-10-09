package com.fedup.shipment

import com.fedup.shared.*
import com.fedup.shared.protocol.*
import com.fedup.shared.protocol.Topics.availableDrivers
import com.fedup.shared.protocol.Topics.driverRequests
import com.fedup.shared.protocol.location.*
import com.fedup.shipment.model.*
import org.apache.kafka.common.serialization.*
import org.springframework.stereotype.*
import java.time.*

/**
 * Responsible for coordination of all shipment operations.
 *
 * Services the following scenario:
 * - A shipper requests a shipment pickup.
 * - The receiver is notified that the shipment is coming and acknowledges that.
 * - The drivers in the vicinity are also notified, the first one to accept gets to pick up the shipment
 *   from the shipper.
 * - The driver meets with the shipper and picks up the shipment.
 * - The receiver of the shipment is notified with the driver's ETA
 * - The driver uses GPS with receiver's location as the destination and delivers the shipment
 * - The receiver acknowledges the delivery, which concludes the process
 *
 * Design and technical details:
 * - Lives in its own bounded context Shipment, which corresponds with the boundary of the component
 * - Shares location-related model with Location Service via shared module. The module, built as a library (jar)
 *   awkwardly plays the role of Shared Kernel pattern. The awkwardness is due to my inability to work around
 *   Kafka's needs to understand messages on both sides of the topics connecting contexts, so there is a need
 *   in some common protocol. If not for that, I would simply place Shipping Service downstream of Location
 *   Service and thought of it as a Collaborator pattern.
 *
 * - Owns (is a single writer to) [shipmentsTopic], [driverRequests] and [notificationsTopic] topics (the latter
 *   topic is read from by User Service that owns push notification machinery)
 * - Reads from [availableDrivers] topic owned by Location Service.
 *   Considers both local tables (they are exposed via views event-sourced by the corresponding streams)
 *
 * - Workflow
 *  - shipper issues "requestShipmentPickup" command.
 *    Shipment is created in PICKUP_REQUESTED state.
 *
 *  - a notification is published for receiver; he issues "acknowledgeUpcomingDelivery" command
 *    Shipment transitions to UPCOMING_DELIVERY_ACKNOWLEDGED_BY_RECEIVER state
 *
 *  - event "NearbyDriversRequested" is published to "driverRequests" topic
 *  - event "DriversLocated" is published to "availableDrivers" topic
 *  - event "Notification" for the located drivers is published to "notifications" topic
 *  - a driver issues "acceptShipmentRequest" command
 *    Shipment transitions to ASSIGNED_TO_DRIVER state
 *
 *  - an event "Notification" is published to "notifications" topic to inform shipper of driver's ETA
 *  - driver issues "reportPickup" command upon getting the shipment from the shipper
 *    Shipment transitions to PICKED_UP_AND_ON_THE_WAY state
 *
 *  - event "Notification" is published to "notifications" topic to inform receiver of driver's ETA
 *  - receiver issues "confirmReceipt" command when driver hands him the shipment
 *    Shipment transitions to DELIVERED state
 */
@Component
class ShippingService(private val shipmentRepository: ShipmentRepository) {

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

    fun checkProgressFor(trackingId: TrackingId): List<ShipmentHistoryRecord>? =
        shipmentRepository.findBy(trackingId)?.history

    fun acknowledgeUpcomingDelivery(trackingId: TrackingId, receiver: Receiver, location: Location) {
        val shipment = shipmentRepository.findBy(trackingId)
        shipment?.let { shipmentRepository.save(shipment.acknowledgedByReceiver(receiver, at = SpaceTimeCoordinates(location))) }
            ?: UnknownShipmentException(trackingId)
    }

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

    companion object {
        val shipmentsTopic = Topic("shipments", Serdes.String(), ShipmentSerde())
        val notificationsTopic = Topic("notifications", Serdes.String(), ShipmentSerde())
    }
}

class UnknownShipmentException(trackingId: TrackingId) : Exception("Unknown tracking id $trackingId")


class ShipmentSerde: Serde<Shipment> {
    private val serializer = ShipmentSerializer()
    private val deserializer = ShipmentDeserializer()

    override fun deserializer(): Deserializer<Shipment> = deserializer
    override fun serializer(): Serializer<Shipment> = serializer

    override fun close() {}

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        serializer.configure(configs, isKey)
        deserializer.configure(configs, isKey)
    }

    class ShipmentDeserializer: Deserializer<Shipment> {
        override fun deserialize(topic: String, data: ByteArray): Shipment =
            Shipment.fromBytes(data)

        override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
        override fun close() {}
    }

    class ShipmentSerializer: Serializer<Shipment> {
        override fun serialize(topic: String, data: Shipment): ByteArray = data.asBytes()

        override fun configure(config: MutableMap<String, *>, isKey: Boolean) {}
        override fun close() {}
    }
}


