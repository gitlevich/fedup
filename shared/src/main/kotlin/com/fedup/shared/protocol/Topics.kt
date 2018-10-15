package com.fedup.shared.protocol

import com.fedup.shared.protocol.location.*
import com.fedup.shared.protocol.shipment.*
import org.apache.kafka.common.serialization.*

class Topic<K, V>(val name: String, val keySerde: Serde<K>, val valueSerde: Serde<V>) {
    override fun toString(): String = name
}

object Topics {
    val driverRequests = Topic("driver-requests", ShipmentSerdes.trackingIdSerde, LocationSerdes.driverRequested)
    val availableDrivers = Topic("available-drivers", ShipmentSerdes.trackingIdSerde, LocationSerdes.driversLocated)
    val userLocations = Topic("user-locations", Serdes.String(), LocationSerdes.userLocation)
}