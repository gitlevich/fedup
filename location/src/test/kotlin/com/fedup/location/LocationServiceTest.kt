package com.fedup.location

import com.fedup.shared.machinery.KafkaStreamsConfig
import com.fedup.shared.protocol.Topics
import com.fedup.shared.protocol.location.*
import com.nhaarman.mockito_kotlin.mock
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.integration.utils.*
import org.apache.kafka.test.StreamsTestUtils
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.*
import java.util.*
import java.util.concurrent.TimeUnit

class LocationServiceTest {

    @Test
    fun `should find stored user location`() {
        val location = UserLocation("driver@drivers.com", Location(37.7534327, -122.4344288), UserRole.DRIVER)
        locationService.recordUserLocation(location)

        await().atMost(1, TimeUnit.SECONDS).untilAsserted {
            assertThat(locationService.locateUser(location.userId))
                .isEqualTo(location)
        }
    }

    /* * * * * * * * * * * * * * * * * *   M A C H I N E R Y   * * * * * * * * * * * * * * * * */
    private lateinit var locationService: LocationService
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

        locationService = LocationService(LocationService.topology(mapService), KafkaStreamsConfig(properties))
    }

    @After
    fun tearDown() {
        locationService.close()
    }

    companion object {
        @ClassRule
        @JvmField
        val EMBEDDED_KAFKA = EmbeddedKafkaCluster(1)

        @BeforeClass
        fun setUpAll() {
            EMBEDDED_KAFKA.createTopic(Topics.userLocations.name)
            EMBEDDED_KAFKA.createTopic(Topics.driverRequests.name)
            EMBEDDED_KAFKA.createTopic(Topics.availableDrivers.name)
        }
    }
}
