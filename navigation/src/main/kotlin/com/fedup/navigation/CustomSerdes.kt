package com.fedup.navigation

import com.fasterxml.jackson.databind.type.*
import com.fasterxml.jackson.module.kotlin.*
import com.fedup.common.*
import org.apache.kafka.common.serialization.*

object CustomSerdes {
    val usersWithDistanceSerde = UsersWithDistanceSerde()
    val locationSerde = LocationSerde()
    val commandSerde = NearbyDriversRequestedSerde()
    val driversLocated = DriversLocatedSerde()
}

val objectMapper = jacksonObjectMapper()


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
        objectMapper.readValue(data, DriversLocated::class.java)

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}

class DriversLocatedSerializer : Serializer<DriversLocated> {
    override fun serialize(topic: String, data: DriversLocated): ByteArray = objectMapper.writeValueAsBytes(data)

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
        objectMapper.readValue(data, NearbyDriversRequested::class.java)

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}

class NearbyDriversRequestedSerializer() : Serializer<NearbyDriversRequested> {
    override fun serialize(topic: String, data: NearbyDriversRequested): ByteArray = objectMapper.writeValueAsBytes(data)

    override fun configure(config: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}


class UsersWithDistanceSerde : Serde<List<UserWithDistance>> {
    private val serializer = UsersWithDistanceSerializer()
    private val deserializer = UsersWithDistanceDeserializer()

    override fun deserializer(): Deserializer<List<UserWithDistance>> = deserializer
    override fun serializer(): Serializer<List<UserWithDistance>> = serializer

    override fun close() {}

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        serializer.configure(configs, isKey)
        deserializer.configure(configs, isKey)
    }
}

class UsersWithDistanceDeserializer : Deserializer<List<UserWithDistance>> {
    override fun deserialize(topic: String, data: ByteArray): List<UserWithDistance> =
        objectMapper.readValue(data, type)

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}

    companion object {
        val type: CollectionType = TypeFactory.defaultInstance().constructCollectionType(List::class.java, UserWithDistance::class.java)
    }
}

class UsersWithDistanceSerializer : Serializer<List<UserWithDistance>> {
    override fun serialize(topic: String, data: List<UserWithDistance>): ByteArray {
        return objectMapper.writeValueAsBytes(data)
    }

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}

class LocationSerde() : Serde<Location> {
    private val serializer = LocationSerializer()
    private val deserializer = LocationDeserializer()

    override fun deserializer(): Deserializer<Location> = deserializer
    override fun serializer(): Serializer<Location> = serializer

    override fun close() {}

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        serializer.configure(configs, isKey)
        deserializer.configure(configs, isKey)
    }
}

class LocationSerializer() : Serializer<Location> {
    override fun serialize(topic: String, data: Location): ByteArray = objectMapper.writeValueAsBytes(data)

    override fun configure(config: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}

class LocationDeserializer() : Deserializer<Location> {
    override fun deserialize(topic: String, data: ByteArray): Location =
        objectMapper.readValue(data, Location::class.java)

    override fun configure(config: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}

}