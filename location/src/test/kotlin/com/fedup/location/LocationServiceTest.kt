package com.fedup.location

import com.fedup.location.LocationEventGenerator.generateUserLocations
import com.fedup.shared.machinery.*
import com.fedup.shared.protocol.*
import com.fedup.shared.protocol.location.*
import com.fedup.shared.protocol.shipment.*
import com.nhaarman.mockito_kotlin.*
import org.apache.kafka.common.serialization.*
import org.apache.kafka.common.utils.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.integration.utils.*
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils.*
import org.apache.kafka.streams.state.*
import org.apache.kafka.test.*
import org.assertj.core.api.Assertions.*
import org.junit.*
import java.time.*
import java.util.*

class LocationServiceTest {

    @Test
    fun `should publish DriversLocated event to available-drivers topic upon receiving NearbyDriversRequested`() {
        given(mapService.findNearestUsers(any(), any(), any())).willReturn(driversWithDistance)

        val driverRequest = LocationEventGenerator.generateDriverRequests(howMany = 1).first()
        produceToDriverRequests(listOf<KeyValue<TrackingId, NearbyDriversRequested>>(KeyValue(driverRequest.trackingId, driverRequest)))

        val availableDrivers = consumeFromAvailableDrivers(Duration.ofSeconds(1), 1)

        assertThat(availableDrivers)
            .hasSize(1)
            .containsExactly(DriversLocated(driverRequest.trackingId, driversWithDistance))
    }


    @Test
    fun `should find stored user location`() {
        val original = UserLocation("driver@drivers.com", Location(37.7534327, -122.4344288), UserRole.DRIVER)
        locationService.recordUserLocation(original)

        val retrieved = locationService.locateUser(original.userId)
        assertThat(retrieved).isEqualTo(original)
    }

    @Test
    fun `should find all local drivers`() {
        val userLocations = generateUserLocations(1)
        produceValuesSynchronously(Topics.userLocations.name,
            userLocations,
            TestUtils.producerConfig(EMBEDDED_KAFKA.bootstrapServers(),
                Topics.userLocations.keySerde.serializer().javaClass,
                Topics.userLocations.valueSerde.serializer().javaClass),
            mockTime)

        val closestLocations = locationService.findActiveDrivers()

        assertThat(closestLocations).isNotEmpty
    }

    /* * * * * * * * * * * * * * * * * *   M A C H I N E R Y   * * * * * * * * * * * * * * * * */
    private lateinit var kafkaStreams: KafkaStreams
    private lateinit var locationService: LocationService
    private val mockTime = Time.SYSTEM
    private val mapService = mock<MapsIntegrationService>()

    @Before
    fun setUp() {
        val properties = StreamsTestUtils.getStreamsConfig(
            "integrationTest",
            EMBEDDED_KAFKA.bootstrapServers(),
            Serdes.String().javaClass.name,
            Serdes.String().javaClass.name,
            Properties())
        properties[IntegrationTestUtils.INTERNAL_LEAVE_GROUP_ON_CLOSE] = true

        locationService = LocationService(mapService, KafkaStreamsConfig(properties))
        kafkaStreams = KafkaStreams(LocationService.locationServiceTopology(mapService) { emptyList() }.build(), properties)
        kafkaStreams.start()
    }

    @After
    fun tearDown() {
        kafkaStreams.close()
    }

    companion object {
        @ClassRule @JvmField val EMBEDDED_KAFKA = EmbeddedKafkaCluster(1)

        @BeforeClass
        fun setUpAll() {
            EMBEDDED_KAFKA.createTopic(Topics.userLocations.name)
            EMBEDDED_KAFKA.createTopic(Topics.driverRequests.name)
            EMBEDDED_KAFKA.createTopic(Topics.availableDrivers.name)
        }

        private val driversWithDistance = listOf(
            UserWithDistance("john", "11 mins (9.4 km)", DistanceInMeters(9417), Duration.ofSeconds(676)),
            UserWithDistance("jane", "13 mins (4.0 km)", DistanceInMeters(3993), Duration.ofSeconds(777)),
            UserWithDistance("vlad", "14 mins (9.4 km)", DistanceInMeters(9404), Duration.ofSeconds(819)),
            UserWithDistance("arnold", "1 hour 6 mins (104 km)", DistanceInMeters(103602), Duration.ofSeconds(3966)),
            UserWithDistance("elon", "1 hour 12 mins (113 km)", DistanceInMeters(112851), Duration.ofSeconds(4314))
        )

        private fun consumeFromAvailableDrivers(waitFor: Duration, maxMessages: Int): List<DriversLocated> =
            IntegrationTestUtils.readValues<DriversLocated>(
                Topics.availableDrivers.name,
                TestUtils.consumerConfig(EMBEDDED_KAFKA.bootstrapServers(),
                    Topics.availableDrivers.keySerde.deserializer().javaClass,
                    Topics.availableDrivers.valueSerde.deserializer().javaClass),
                waitFor.toMillis(),
                maxMessages
            )

        private fun produceToDriverRequests(requests: List<KeyValue<TrackingId, NearbyDriversRequested>>) {
            produceKeyValuesSynchronously(Topics.driverRequests.name,
                requests,
                TestUtils.producerConfig(EMBEDDED_KAFKA.bootstrapServers(),
                    Topics.driverRequests.keySerde.serializer().javaClass,
                    Topics.driverRequests.valueSerde.serializer().javaClass),
                Time.SYSTEM
            )
        }
    }
}
