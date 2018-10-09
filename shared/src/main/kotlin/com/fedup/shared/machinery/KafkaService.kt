package com.fedup.shared.machinery

import kotlin.concurrent.*

interface KafkaService: AutoCloseable {
    override fun close()

}

fun addShutdownHook(service: KafkaService) {
    Thread.currentThread().setUncaughtExceptionHandler { _, _ -> service.close() }
    Runtime.getRuntime().addShutdownHook(
        thread(
            start = false,
            block = {
                service.close()
                Thread.sleep(1000)
            }
        ))
}
