package com.fedup.shipping

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BookingApp(private val shippingService: ShippingService) {
    fun run() {
        shippingService.start()
    }
}

fun main(args: Array<String>) {
    runApplication<BookingApp>(*args)
}
