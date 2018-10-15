package com.fedup.location

import com.fedup.shared.machinery.*
import com.fedup.shared.protocol.*
import com.fedup.shared.protocol.location.*
import com.fedup.shared.protocol.shipment.*
import org.apache.kafka.clients.producer.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.*
import java.time.Duration
import java.util.concurrent.*
import kotlin.math.*

object LocationEventGenerator {

    private val locationBounds = Location(37.7534327, -122.4344288) to Location(37.726768, -122.390035)

    fun generateDriverLocations(howMany: Int): List<UserLocation> =
        (0..howMany).map { UserLocation(makeDriverId(), STC(randomLocationWithinBounds(locationBounds)), UserRole.DRIVER) }

    fun generateDriversLocatedEvents(trackingId: TrackingId = TrackingId.next(), howMany: Int = 1): List<DriversLocated> {
        val distance = DistanceInMeters(randomIntBetween(100, 5000))
        val duration = Duration.ofSeconds(distance.distance/100)
        return (0..howMany).map { DriversLocated(trackingId, listOf(UserWithDistance(makeDriverId(), "", distance, duration))) }
    }

    fun generateDriverRequests(trackingId: TrackingId = TrackingId.next(), howMany: Int = 1): List<NearbyDriversRequested> =
        (0..howMany).map { NearbyDriversRequested(trackingId, randomLocationWithinBounds(locationBounds)) }

    fun generateUserLocations(howMany: Int = 1): List<UserLocation> =
        (0..howMany).map { UserLocation("user_$it", randomLocationWithinBounds(locationBounds), UserRole.DRIVER) }

    fun createDriverRequestStream(events: List<NearbyDriversRequested>): KafkaStreams {
        val builder = StreamsBuilder()

        builder.stream(
            Topics.driverRequests.name,
            Consumed.with(Topics.driverRequests.keySerde, Topics.driverRequests.valueSerde)
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

    private fun makeDriverId() = "driver_${randomIntBetween(1, 1000)}"

    private fun randomIntBetween(start: Long, end: Long) = ThreadLocalRandom.current().nextLong(start, end)
}

fun main(args: Array<String>) {
    val records = LocationEventGenerator.generateDriverRequests(TrackingId("444-433-222"), 3)
        .map { event -> ProducerRecord(Topics.driverRequests.name, event.trackingId, event) }
    send(records, Topics.driverRequests)
}

