package com.fedup.navigation

import org.apache.kafka.common.serialization.*

object CustomSerdes {
    val commandSerde = NearbyDriversRequestedSerde()
    val driversLocated = DriversLocatedSerde()
}

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
