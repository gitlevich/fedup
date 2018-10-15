package com.fedup.shared.protocol.location

import org.apache.kafka.common.serialization.*

object LocationSerdes {
    val driverRequested: Serde<NearbyDriversRequested> = Serdes.serdeFrom(NearbyDriversRequestedSerializer(), NearbyDriversRequestedDeserializer())
    val driversLocated: Serde<DriversLocated> = Serdes.serdeFrom(DriversLocatedSerializer(), DriversLocatedDeserializer())
    val userLocation: Serde<UserLocation> = Serdes.serdeFrom(UserLocationSerializer(), UserLocationDeserializer())
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