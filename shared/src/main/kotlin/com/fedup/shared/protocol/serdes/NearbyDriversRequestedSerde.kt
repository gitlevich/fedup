package com.fedup.shared.protocol.serdes

import com.fedup.shared.protocol.location.*
import org.apache.kafka.common.serialization.*

class NearbyDriversRequestedSerde : Serde<NearbyDriversRequested> {
    private val serializer = NearbyDriversRequestedSerializer()
    private val deserializer = NearbyDriversRequestedDeserializer()

    override fun deserializer(): Deserializer<NearbyDriversRequested> = deserializer
    override fun serializer(): Serializer<NearbyDriversRequested> = serializer

    override fun close() {}

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        serializer.configure(configs, isKey)
        deserializer.configure(configs, isKey)
    }
}

class NearbyDriversRequestedDeserializer : Deserializer<NearbyDriversRequested> {
    override fun deserialize(topic: String, data: ByteArray): NearbyDriversRequested =
        NearbyDriversRequested.fromBytes(data)

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}

class NearbyDriversRequestedSerializer() : Serializer<NearbyDriversRequested> {
    override fun serialize(topic: String, data: NearbyDriversRequested): ByteArray =
        data.asBytes()

    override fun configure(config: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}
