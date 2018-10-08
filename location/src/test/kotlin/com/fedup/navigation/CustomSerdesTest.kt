package com.fedup.navigation

import com.fedup.shared.*
import org.assertj.core.api.Assertions.*
import org.junit.*

class CustomSerdesTest {
    private val topic = "blah"

    @Test
    fun `should serialize command NearbyDriversRequested and deserialize it back to the original`() {
        val original = NearbyDriversRequested(TrackingId("123"), Location(37.7724868, -122.4166086))

        val serialized = CustomSerdes.commandSerde.serializer().serialize(topic, original)
        val deserialized = CustomSerdes.commandSerde.deserializer().deserialize(topic, serialized)

        assertThat(deserialized).isEqualTo(original)
    }

    @Test
    fun `should serialize DriversLocated and deserialize it back to the original`() {
        val original = DriversLocated(
            TrackingId("123"),
            listOf(UserWithDistance("user1", "too far and too long"), UserWithDistance("user2", "even further and longer"))
        )

        val serialized = CustomSerdes.driversLocated.serializer().serialize(topic, original)
        val deserialized = CustomSerdes.driversLocated.deserializer().deserialize(topic, serialized)

        assertThat(deserialized).isEqualTo(original)
    }

    @Test
    fun `extensions should work`() {
        val original = NearbyDriversRequested(TrackingId("123"), Location(37.7724868, -122.4166086))

        val serialized = original.asString()
        val deserialized = NearbyDriversRequested.fromString(serialized)

        assertThat(deserialized).isEqualTo(original).also { println(serialized) }
    }
}
