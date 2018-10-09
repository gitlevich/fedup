package com.fedup.shared.machinery

import kotlin.concurrent.*

interface KafkaService {
    fun start()
    fun stop()

}

fun addShutdownHook(service: KafkaService) {
    Thread.currentThread().setUncaughtExceptionHandler { _, _ -> service.stop() }
    Runtime.getRuntime().addShutdownHook(
        thread(
            start = false,
            block = {
                service.stop()
                Thread.sleep(1000)
            }
        ))
}
