package com.fedup.common

typealias UserId = String

data class Location(val latitude: Double, val longitude: Double) {
    override fun toString(): String = "$latitude, $longitude"
}

