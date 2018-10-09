package com.fedup.shipment.model

import com.fedup.shared.Characterization.Entity

sealed class User: Entity<String>()
data class Shipper(override val identity: String): User()
data class Receiver(override val identity: String): User()
data class Driver(override val identity: String): User()

