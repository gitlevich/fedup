package com.fedup.user

import com.fedup.shared.protocol.shipment.*

class PaymentService {
    /**
     * Accepts and processes a payment from the specified user via the given payment method
     * After success or failure Publishes a corresponding PaymentEvent to the payments topic
     */
    fun acceptUserPayment(user: UserId, payment: Payment) {
        TODO()
    }
}

data class Money(val amount: Double, val currency: String)
data class Payment(val transactionId: String, val amount: Money, val paymentMethod: PaymentMethod)
/**
 * Contains all necessary information required by the provider of this payment service. Examples:
 * - credit card
 * - bank account
 * - PayPal
 */
sealed class PaymentMethod


sealed class PaymentEvent() {
    abstract val transactionId: String
}
data class PaymentRequested(override val transactionId: String, val payment: Payment): PaymentEvent()
data class PaymentAccepted(override val transactionId: String): PaymentEvent()
data class PaymentRejected(override val transactionId: String, val reason: String): PaymentEvent()
