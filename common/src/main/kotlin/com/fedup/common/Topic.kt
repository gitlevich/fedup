package com.fedup.common

import org.apache.kafka.common.serialization.*


class Topic<K, V>(val name: String, val keySerde: Serde<K>, val valueSerde: Serde<V>) {
    override fun toString(): String = name
}

object Topics {
    val shipments = Topic("shipments", Serdes.String(), Serdes.String())
    val userLocations = Topic("user-locations", Serdes.String(), Serdes.String())
    val userNotifications = Topic("user-notification", Serdes.String(), Serdes.String())
}