package com.fedup.shipping

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BookingApp

fun main(args: Array<String>) {
    runApplication<BookingApp>(*args)
}
