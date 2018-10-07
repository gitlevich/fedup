package com.fedup.user

import org.dddcommunity.*
import java.time.*

data class Address(val street: String, val city: String, val state: String, val zip: String)
data class EmailAddress(val address: String)
sealed class PaymentMethod
data class CreditCard(val number: Int, val expiration: LocalDate, val billingAddress: Address)
data class BankAccount(val rtn: String, val number: String, val billingAddress: Address)

data class User(
    val email: EmailAddress,
    val name: String,
    val paymentMethod: PaymentMethod
): Entity<EmailAddress> {
    override val identity = email
}

/**
 * Subscribes to user-notifications stream and pushes events from this stream to the users they are addressed to
 */
class UserNotificationService {

    /**
     * Pushes the specified notification to the user's device
     */
    fun notifyUser(user: User, notification: Any) {

    }
}

class UserService(private val userRepository: UserRepository) {
    fun register(user: User) {
        userRepository.save(user)
    }
}

/**
 * Sits on top of Kafka stream users and some event-sourced views
 */
class UserRepository {
    fun save(user: User) {
        TODO()
    }
}

class PaymentService {
}