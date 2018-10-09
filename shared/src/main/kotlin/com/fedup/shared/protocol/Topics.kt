package com.fedup.shared.protocol

import com.fedup.shared.protocol.location.*
import org.apache.kafka.common.serialization.*

class Topic<K, V>(val name: String, val keySerde: Serde<K>, val valueSerde: Serde<V>) {
    override fun toString(): String = name
}

object Topics {
    val locationRequests = Topic("location-requests", LocationSerdes.trackingIdSerde, LocationSerdes.commandSerde)
    val availableDrivers = Topic("available-drivers", LocationSerdes.trackingIdSerde, LocationSerdes.driversLocated)
    val userLocations = Topic("user-locations", Serdes.String(), LocationSerdes.userLocation)

}