package com.fedup.common.machinery

import kotlin.concurrent.*

interface Service {
    fun start()
    fun stop()

}

fun addShutdownHook(service: Service) {
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
