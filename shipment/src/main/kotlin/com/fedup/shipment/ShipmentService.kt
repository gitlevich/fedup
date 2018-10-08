package com.fedup.shipment

import com.fedup.common.machinery.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.*
import org.springframework.boot.autoconfigure.*
import org.springframework.context.annotation.*

/**
 * Subscribes to command stream, where it listens for relevant commands, like [ShipmentRequest].
 * Owns shipments topic.
 *
 * TODO create the topics it writes to (single-writer) and tables it uses; define its topics here instead of common
 */
@SpringBootApplication
class ShippingService

@Configuration
class ApConfig {
    @Bean
    fun streamsConfig(@Value("\${kafka.bootstrap.servers}") bootstrapServers: String,
                      @Value("\${kafka.state.dir}") stateDir: String,
                      @Value("\${kafka.enableEOS}") enableEOS: Boolean): KafkaStreamsConfig =
        createStreamsConfig(ShippingService::class.simpleName!!, bootstrapServers, stateDir, enableEOS)
}

fun main(args: Array<String>) {
    runApplication<ShippingService>(*args)
}
