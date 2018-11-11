package com.fedup.location

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.*
import com.fedup.shared.machinery.*
import org.apache.kafka.streams.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.*
import org.springframework.boot.autoconfigure.*
import org.springframework.context.annotation.*

@SpringBootApplication
class LocationServiceApp(locationService: LocationService) {
    init {
        addShutdownHook(locationService)
    }
}

@Configuration
class ApConfig(objectMapper: ObjectMapper) {
    init {
        objectMapper.registerModule(JavaTimeModule())
    }

    @Bean fun googleApiKey(@Value("\${googlemaps.api.key}") apiKey: String): String = apiKey

    @Bean fun streamsConfig(@Value("\${kafka.bootstrap.servers}") bootstrapServers: String,
                            @Value("\${kafka.state.dir}") stateDir: String,
                            @Value("\${kafka.enableEOS}") enableEOS: Boolean): KafkaStreamsConfig =
        createStreamsConfig(LocationService::class.simpleName!!, bootstrapServers, stateDir, enableEOS)

    @Bean fun topology(mapsIntegrationService: MapsIntegrationService): Topology =
        LocationService.topology(mapsIntegrationService)
}

fun main(args: Array<String>) {
    runApplication<LocationServiceApp>(*args)
}


