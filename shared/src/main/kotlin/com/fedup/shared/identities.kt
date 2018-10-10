package com.fedup.shared

import java.util.*

data class TrackingId(val value: String) {
    companion object {
        fun next() = TrackingId(UUID.randomUUID().toString())
    }
}

typealias UserId = String