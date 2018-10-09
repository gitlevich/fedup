package com.fedup.shared.protocol.serdes

import com.fedup.shared.*
import com.fedup.shared.protocol.location.*
import org.apache.kafka.common.serialization.*

class TrackingIdSerde: Serde<TrackingId> {
    private val serializer = TrackingIdSerializer()
    private val deserializer = TrackingIdDeserializer()

    override fun deserializer(): Deserializer<TrackingId> = deserializer
    override fun serializer(): Serializer<TrackingId> = serializer

    override fun close() {}

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        serializer.configure(configs, isKey)
        deserializer.configure(configs, isKey)
    }
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
