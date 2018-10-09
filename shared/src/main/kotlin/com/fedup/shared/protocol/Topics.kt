package com.fedup.shared.protocol

import org.apache.kafka.common.serialization.*

class Topic<K, V>(val name: String, val keySerde: Serde<K>, val valueSerde: Serde<V>) {
    override fun toString(): String = name
}

object Topics {
    val driverRequests = Topic("driver-requests", CustomSerdes.trackingIdSerde, CustomSerdes.commandSerde)
    val availableDrivers = Topic("available-drivers", CustomSerdes.trackingIdSerde, CustomSerdes.driversLocated)
    val userLocations = Topic("user-locations", Serdes.String(), CustomSerdes.userLocation)
}