package com.fedup.location

import com.fedup.shared.*
import com.fedup.shared.machinery.*
import com.fedup.shared.protocol.*
import com.fedup.shared.protocol.location.*
import org.apache.kafka.clients.producer.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.*
import java.util.concurrent.*
import kotlin.math.*

object LocationEventGenerator {

    private val locationBounds = Location(37.7534327, -122.4344288) to Location(37.726768, -122.390035)

    fun generateDrivers(howMany: Int) =
        (0..howMany).map { UserLocation("driver_${ThreadLocalRandom.current().nextInt(1, howMany * 10)}", randomLocationWithinBounds(locationBounds)) }

    fun generateDriverRequests(trackingId: TrackingId, howMany: Int): List<NearbyDriversRequested> =
        (0..howMany).map { NearbyDriversRequested(trackingId, randomLocationWithinBounds(locationBounds)) }

    fun createDriverRequestStream(events: List<NearbyDriversRequested>): KafkaStreams {
        val builder = StreamsBuilder()

        builder.stream(
            Topics.locationRequests.name,
            Consumed.with(Topics.locationRequests.keySerde, Topics.locationRequests.valueSerde)
        )

        return KafkaStreams(builder.build(), createStreamsConfig(
            LocationEventGenerator::class.simpleName!!,
            "localhost:29092",
            "/tmp/kafka-streams",
            true
        ).props)
    }

    private fun randomLocationWithinBounds(bounds: Pair<Location, Location>): Location {
        val latRange = bounds.first.latitude..bounds.second.latitude
        val lngRange = bounds.first.longitude..bounds.second.longitude

        val generatedLat = ThreadLocalRandom.current().nextDouble(
            min(latRange.start, latRange.endInclusive),
            max(latRange.start, latRange.endInclusive)
        )
        val generatedLng = ThreadLocalRandom.current().nextDouble(
            min(lngRange.start, lngRange.endInclusive),
            max(lngRange.start, lngRange.endInclusive)
        )

        return Location(generatedLat, generatedLng)
    }
}

fun main(args: Array<String>) {
    LocationEventGenerator.generateDriverRequests(TrackingId("444-433-222"), 3).forEach { println(it) }
    LocationEventGenerator.generateDrivers(3).forEach { println(it) }

    val records = LocationEventGenerator.generateDriverRequests(TrackingId("444-433-222"), 3)
        .map { event -> ProducerRecord(Topics.locationRequests.name, event.trackingId, event) }
    send(records, Topics.locationRequests)
}

