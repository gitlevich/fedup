package com.fedup.shared.protocol

import com.fedup.shared.protocol.serdes.*

object CustomSerdes {
    val trackingIdSerde = TrackingIdSerde()
    val commandSerde = NearbyDriversRequestedSerde()
    val driversLocated = DriversLocatedSerde()
    val userLocation = UserLocationSerde()
}