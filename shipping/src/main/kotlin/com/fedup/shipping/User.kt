package com.fedup.shipping

import com.fedup.common.*

sealed class User: Entity<String>()
data class Shipper(override val identity: String): User()
data class Receiver(override val identity: String): User()
data class Driver(override val identity: String): User()

