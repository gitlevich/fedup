package com.fedup.navigation

import org.springframework.beans.factory.annotation.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.*

@SpringBootApplication
@PropertySource("/application.properties")
class NavigationApp {
    @Bean fun googleApiKey(@Value("\${googlemaps.api.key}") apiKey: String): String = apiKey
}

fun main(args: Array<String>) {
    runApplication<NavigationApp>(*args)
}
