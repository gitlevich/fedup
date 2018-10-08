package com.fedup.navigation

import com.fedup.shared.machinery.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.*
import org.springframework.boot.autoconfigure.*
import org.springframework.context.annotation.*

@SpringBootApplication
class LocationServiceApp(locationService: LocationService) {
    init {
        locationService.start()
        addShutdownHook(locationService)
    }
}

@Configuration
class ApConfig {
    @Bean fun googleApiKey(@Value("\${googlemaps.api.key}") apiKey: String): String = apiKey

    @Bean fun streamsConfig(@Value("\${kafka.bootstrap.servers}") bootstrapServers: String,
                            @Value("\${kafka.state.dir}") stateDir: String,
                            @Value("\${kafka.enableEOS}") enableEOS: Boolean): KafkaStreamsConfig =
        createStreamsConfig(LocationService::class.simpleName!!, bootstrapServers, stateDir, enableEOS)
}

fun main(args: Array<String>) {
    runApplication<LocationServiceApp>(*args)
}


