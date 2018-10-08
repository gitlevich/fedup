package com.fedup.user

import org.dddcommunity.Entity

data class Address(val street: String, val city: String, val state: String, val zip: String)
data class EmailAddress(val address: String)

data class User(
    val email: EmailAddress,
    val name: String,
    val address: Address,
    val paymentMethod: PaymentMethod
): Entity<EmailAddress> {
    override val identity = email
}


