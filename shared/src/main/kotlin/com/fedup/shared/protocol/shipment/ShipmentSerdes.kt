package com.fedup.shared.protocol.shipment

import com.fedup.shared.protocol.location.*
import org.apache.kafka.common.serialization.*

object ShipmentSerdes {
    val trackingIdSerde: Serde<TrackingId> = Serdes.serdeFrom(TrackingIdSerializer(), TrackingIdDeserializer())
}


class TrackingIdDeserializer: Deserializer<TrackingId> {
    override fun deserialize(topic: String, data: ByteArray): TrackingId =
        TrackingId.fromBytes(data)

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}

class TrackingIdSerializer: Serializer<TrackingId> {
    override fun serialize(topic: String, data: TrackingId): ByteArray = data.asBytes()

    override fun configure(config: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}

