package com.fedup.shared.protocol.location

import com.fedup.shared.protocol.shipment.*
import org.assertj.core.api.Assertions.*
import org.junit.*
import java.time.*

class CustomSerdesTest {
    private val topic = "blah"

    @Test
    fun `should serialize NearbyDriversRequested and deserialize it back to the original`() {
        val original = NearbyDriversRequested(TrackingId("123"), Location(37.7724868, -122.4166086))

        val serialized = LocationSerdes.driverRequested.serializer().serialize(topic, original)
        val deserialized = LocationSerdes.driverRequested.deserializer().deserialize(topic, serialized)

        assertThat(deserialized).isEqualTo(original)
    }

    @Test
    fun `should serialize DriversLocated and deserialize it back to the original`() {
        val original = DriversLocated(
            TrackingId("123"),
            listOf(
                UserWithDistance("user1", "too far and too long", DistanceInMeters(1200), Duration.ofMinutes(10)),
                UserWithDistance("user2", "even further and longer", DistanceInMeters(1480), Duration.ofMinutes(12))
            )
        )

        val serialized = LocationSerdes.driversLocated.serializer().serialize(topic, original)
        val deserialized = LocationSerdes.driversLocated.deserializer().deserialize(topic, serialized)

        assertThat(deserialized).isEqualTo(original)
    }

    @Test
    fun `extensions should work`() {
        val original = NearbyDriversRequested(TrackingId("123"), Location(37.7724868, -122.4166086))

        val serialized = original.asJson()
        val deserialized = NearbyDriversRequested.fromJson(serialized)

        assertThat(deserialized).isEqualTo(original)
    }
}
