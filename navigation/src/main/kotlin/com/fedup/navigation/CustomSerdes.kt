package com.fedup.navigation

import com.fedup.common.*
import org.apache.kafka.common.serialization.*

object CustomSerdes {
    class UsersWithDistanceSerde : Serde<List<UserWithDistance>> {
        private val serializer = UserWithDistanceSerializer()
        private val deserializer = UserWithDistanceDeserializer()

        override fun deserializer(): Deserializer<List<UserWithDistance>> = deserializer
        override fun serializer(): Serializer<List<UserWithDistance>> = serializer

        override fun close() {}

        override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
            serializer.configure(configs, isKey)
            deserializer.configure(configs, isKey)
        }}

    class UserWithDistanceDeserializer : Deserializer<List<UserWithDistance>> {
        override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
        }

        override fun deserialize(topic: String, data: ByteArray): List<UserWithDistance> {
            TODO("not implemented")
        }

        override fun close() {
        }

    }

    class UserWithDistanceSerializer : Serializer<List<UserWithDistance>> {
        override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {
            TODO("not implemented")
        }

        override fun serialize(topic: String, data: List<UserWithDistance>): ByteArray {
            TODO("not implemented")
        }

        override fun close() {
        }

    }

    class LocationSerde: Serde<Location> {
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

    class LocationSerializer : Serializer<Location> {
        override fun configure(config: MutableMap<String, *>?, isKey: Boolean) {
            TODO("not implemented")
        }

        override fun serialize(topic: String?, data: Location): ByteArray {
            TODO("not implemented")
        }

        override fun close() {
        }

    }

    class LocationDeserializer : Deserializer<Location> {
        override fun configure(config: MutableMap<String, *>, isKey: Boolean) {
            TODO("not implemented")
        }

        override fun deserialize(topic: String, data: ByteArray): Location {
            TODO("not implemented")
        }

        override fun close() {
        }

    }
}