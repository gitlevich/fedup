package com.fedup.location

import com.fedup.location.LocationService.Companion.userLocationStore
import com.fedup.shared.protocol.Topics.availableDrivers
import com.fedup.shared.protocol.Topics.driverRequests
import com.fedup.shared.protocol.Topics.userLocations
import com.fedup.shared.protocol.location.*
import com.fedup.shared.protocol.shipment.TrackingId
import com.nhaarman.mockito_kotlin.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.*
import org.apache.kafka.streams.processor.WallclockTimestampExtractor
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.junit.rules.TemporaryFolder
import java.time.Duration
import java.util.*


class LocationServiceTopologyTest {

    @Test
    fun `location service topology should store driver locations`() {
        val topology = LocationService.topology(mapService)
        val streams = TopologyTestDriver(topology, streamsProperties)

        val driver1Location = UserLocation("driver1@drivers.com", Location(37.7534327, -122.4344288), UserRole.DRIVER)
        streams.pipeInput(userLocationsRecordFactory.create(userLocations.name, driver1Location.userId, driver1Location))

        val driver2Location = UserLocation("driver2@drivers.com", Location(39.3453987, -122.2828282), UserRole.DRIVER)
        streams.pipeInput(userLocationsRecordFactory.create(userLocations.name, driver2Location.userId, driver2Location))

        val locationStore = streams.getKeyValueStore<String, UserLocation>(userLocationStore)
        assertThat(driver1Location).isEqualTo(locationStore[driver1Location.userId])
        assertThat(driver2Location).isEqualTo(locationStore[driver2Location.userId])
    }

    @Test
    fun `location service topology should find available drivers for a driver request when some are available`() {
        val topology = LocationService.topology(mapService)
        val streams = TopologyTestDriver(topology, streamsProperties)

        val originalLocation = UserLocation("driver@drivers.com", Location(37.7534327, -122.4344288), UserRole.DRIVER)
        streams.pipeInput(userLocationsRecordFactory.create(userLocations.name, originalLocation.userId, originalLocation))

        val driverRequest = LocationEventGenerator.generateDriverRequests(howMany = 1).first()
        streams.pipeInput(driverRequestFactory.create(driverRequests.name, driverRequest.trackingId, driverRequest))

        val availableDrivers = streams.readOutput(availableDrivers.name, availableDrivers.keySerde.deserializer(), availableDrivers.valueSerde.deserializer())
        assertThat(availableDrivers.key()).isEqualTo(driverRequest.trackingId)
        assertThat(availableDrivers.value()).isEqualTo(DriversLocated(driverRequest.trackingId, driversWithDistance))
    }

    @Test
    fun `location service topology should find no available drivers for a driver request when none are available`() {
        given(mapService.findNearestUsers(any(), any(), any())).willReturn(emptyList())

        val topology = LocationService.topology(mapService)
        val streams = TopologyTestDriver(topology, streamsProperties)

        val originalLocation = UserLocation("driver@drivers.com", Location(37.7534327, -122.4344288), UserRole.DRIVER)
        streams.pipeInput(userLocationsRecordFactory.create(userLocations.name, originalLocation.userId, originalLocation))

        val driverRequest = LocationEventGenerator.generateDriverRequests(howMany = 1).first()
        streams.pipeInput(driverRequestFactory.create(driverRequests.name, driverRequest.trackingId, driverRequest))

        val availableDrivers = streams.readOutput(availableDrivers.name, availableDrivers.keySerde.deserializer(), availableDrivers.valueSerde.deserializer())
        assertThat(availableDrivers.key()).isEqualTo(driverRequest.trackingId)
        assertThat(availableDrivers.value()).isEqualTo(DriversLocated(driverRequest.trackingId, emptyList()))
    }

    /* * * * * * * * * * * * * * * * * *   M A C H I N E R Y   * * * * * * * * * * * * * * * * */
    private val mapService = mock<MapsIntegrationService>()

    @Rule
    @JvmField
    val stateDirectory = TemporaryFolder()

    private val userLocationsRecordFactory = ConsumerRecordFactory<String, UserLocation>(
        userLocations.name,
        userLocations.keySerde.serializer(),
        userLocations.valueSerde.serializer()
    )
    private val driverRequestFactory = ConsumerRecordFactory<TrackingId, NearbyDriversRequested>(
        driverRequests.name,
        driverRequests.keySerde.serializer(),
        driverRequests.valueSerde.serializer()
    )
    private lateinit var streamsProperties: Properties

    @Before
    fun setUp() {
        streamsProperties = streamsProperties(stateDirectory)
        given(mapService.findNearestUsers(any(), any(), any())).willReturn(driversWithDistance)
    }

    companion object {
        private val driversWithDistance = listOf(
            UserWithDistance("john", "11 mins (9.4 km)", DistanceInMeters(9417), Duration.ofSeconds(676)),
            UserWithDistance("jane", "13 mins (4.0 km)", DistanceInMeters(3993), Duration.ofSeconds(777)),
            UserWithDistance("vlad", "14 mins (9.4 km)", DistanceInMeters(9404), Duration.ofSeconds(819)),
            UserWithDistance("arnold", "1 hour 6 mins (104 km)", DistanceInMeters(103602), Duration.ofSeconds(3966)),
            UserWithDistance("elon", "1 hour 12 mins (113 km)", DistanceInMeters(112851), Duration.ofSeconds(4314))
        )

        private fun streamsProperties(stateDir: TemporaryFolder): Properties = Properties().apply {
            put(StreamsConfig.CLIENT_ID_CONFIG, "test-client")
            put(ConsumerConfig.GROUP_ID_CONFIG, "locations")
            put(StreamsConfig.APPLICATION_ID_CONFIG, "location-service")
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
            put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1)
            put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor::class.java)
            put(StreamsConfig.STATE_DIR_CONFIG, stateDir.root.absolutePath)
        }
    }
}
