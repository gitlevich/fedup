package com.fedup.common

import org.apache.kafka.common.serialization.*


class Topic<K, V>(val name: String, val keySerde: Serde<K>, val valueSerde: Serde<V>) {
    override fun toString(): String = name
}

object Topics {
    val USER_LOCATIONS = Topic("user-locations", Serdes.String(), Serdes.String())
}