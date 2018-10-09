package com.fedup.shared.protocol.location

import com.fedup.shared.*
import org.apache.kafka.common.serialization.*

object LocationSerdes {
    val trackingIdSerde = TrackingIdSerde()
    val commandSerde = NearbyDriversRequestedSerde()
    val driversLocated = DriversLocatedSerde()
    val userLocation = UserLocationSerde()
}

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
