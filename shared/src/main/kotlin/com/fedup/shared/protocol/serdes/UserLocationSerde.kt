package com.fedup.shared.protocol.serdes

import com.fedup.shared.protocol.location.*
import org.apache.kafka.common.serialization.*

class UserLocationSerde: Serde<UserLocation> {
    private val serializer = UserLocationSerializer()
    private val deserializer = UserLocationDeserializer()

    override fun deserializer(): Deserializer<UserLocation> = deserializer
    override fun serializer(): Serializer<UserLocation> = serializer

    override fun close() {}

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        serializer.configure(configs, isKey)
        deserializer.configure(configs, isKey)
    }
}

class UserLocationDeserializer: Deserializer<UserLocation> {
    override fun deserialize(topic: String, data: ByteArray): UserLocation =
        UserLocation.fromBytes(data)

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}

class UserLocationSerializer: Serializer<UserLocation> {
    override fun serialize(topic: String, data: UserLocation): ByteArray = data.asBytes()

    override fun configure(config: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}