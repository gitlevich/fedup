package com.fedup.shared.protocol.serdes

import com.fedup.shared.protocol.location.*
import org.apache.kafka.common.serialization.*

class DriversLocatedSerde: Serde<DriversLocated> {
    private val serializer = DriversLocatedSerializer()
    private val deserializer = DriversLocatedDeserializer()

    override fun deserializer(): Deserializer<DriversLocated> = deserializer
    override fun serializer(): Serializer<DriversLocated> = serializer

    override fun close() {}

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        serializer.configure(configs, isKey)
        deserializer.configure(configs, isKey)
    }
}

class DriversLocatedDeserializer : Deserializer<DriversLocated> {
    override fun deserialize(topic: String, data: ByteArray): DriversLocated =
        DriversLocated.fromBytes(data)

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}

class DriversLocatedSerializer : Serializer<DriversLocated> {
    override fun serialize(topic: String, data: DriversLocated): ByteArray = data.asBytes()

    override fun configure(config: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}