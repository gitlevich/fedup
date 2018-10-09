package com.fedup.shared.protocol.location

import com.fasterxml.jackson.module.kotlin.*
import com.fedup.shared.*

val objectMapper = jacksonObjectMapper()

// Extensions fpr serialization/deserialization
fun NearbyDriversRequested.Companion.fromBytes(bytes: ByteArray): NearbyDriversRequested = objectMapper.readValue(bytes, NearbyDriversRequested::class.java)
fun NearbyDriversRequested.Companion.fromJson(string: String): NearbyDriversRequested = objectMapper.readValue(string, NearbyDriversRequested::class.java)
fun NearbyDriversRequested.asBytes(): ByteArray = objectMapper.writeValueAsBytes(this)
fun NearbyDriversRequested.asJson(): String = objectMapper.writeValueAsString(this)

fun DriversLocated.Companion.fromBytes(bytes: ByteArray): DriversLocated = objectMapper.readValue(bytes, DriversLocated::class.java)
fun DriversLocated.Companion.fromJson(string: String): DriversLocated = objectMapper.readValue(string, DriversLocated::class.java)
fun DriversLocated.asBytes(): ByteArray = objectMapper.writeValueAsBytes(this)
fun DriversLocated.asJson(): String = objectMapper.writeValueAsString(this)

fun UserLocationChanged.Companion.fromBytes(bytes: ByteArray): UserLocationChanged = objectMapper.readValue(bytes, UserLocationChanged::class.java)
fun UserLocationChanged.Companion.fromJson(string: String): UserLocationChanged = objectMapper.readValue(string, UserLocationChanged::class.java)
fun UserLocationChanged.asBytes(): ByteArray = objectMapper.writeValueAsBytes(this)
fun UserLocationChanged.asJson(): String = objectMapper.writeValueAsString(this)

fun UserLocation.Companion.fromBytes(bytes: ByteArray): UserLocation = objectMapper.readValue(bytes, UserLocation::class.java)
fun UserLocation.Companion.fromJson(string: String): UserLocation = objectMapper.readValue(string, UserLocation::class.java)
fun UserLocation.asBytes(): ByteArray = objectMapper.writeValueAsBytes(this)
fun UserLocation.asJson(): String = objectMapper.writeValueAsString(this)

fun TrackingId.Companion.fromBytes(bytes: ByteArray): TrackingId = objectMapper.readValue(bytes, TrackingId::class.java)
fun TrackingId.Companion.fromJson(string: String): TrackingId = objectMapper.readValue(string, TrackingId::class.java)
fun TrackingId.asBytes(): ByteArray = objectMapper.writeValueAsBytes(this)
fun TrackingId.asJson(): String = objectMapper.writeValueAsString(this)

fun Location.asJson(): String = objectMapper.writeValueAsString(this)
