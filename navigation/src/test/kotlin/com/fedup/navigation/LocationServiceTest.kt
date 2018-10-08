package com.fedup.navigation

import com.fedup.shared.*
import com.salesforce.kafka.test.*
import com.salesforce.kafka.test.junit4.*
import org.assertj.core.api.Assertions.*
import org.junit.*
import org.junit.runner.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.context.*
import org.springframework.test.context.junit4.*


@RunWith(SpringRunner::class)
@SpringBootTest
class LocationServiceTest {

    @Test
    fun `should publish DriversLocated event to available-drivers topic upon receiving NearbyDriversRequested request`() {
        val request = NearbyDriversRequested(TrackingId("123"), Location(37.7724868, -122.4166086))
        val locationRequests = mapOf(
            request.trackingId.asBytes() to request.asBytes()
        )

        kafka.produceRecords(locationRequests, locationService.locationRequests.name, 0)
        val records = kafka.consumeAllRecordsFromTopic(locationService.availableDrivers.name)
            .map { record -> TrackingId.fromBytes(record.key()) to DriversLocated.fromBytes(record.value()) }

        assertThat(records).hasSize(1)

        val (key, value) = records[0]
        assertThat(key).isEqualTo(request.trackingId)
        assertThat(value).isEqualTo(request)
    }

    /* * * * * * * * * * * * * * * * * *   M A C H I N E R Y   * * * * * * * * * * * * * * * * */
    @Autowired private lateinit var locationService: LocationService

    @Before
    fun setUp() {
        kafka.createTopic(locationService.locationRequests.name, 1, 1)
        kafka.createTopic(locationService.availableDrivers.name, 1, 1)

        locationService.start()
    }

    @After
    fun tearDown() {
        locationService.stop()
    }

    companion object {
        @ClassRule @JvmField public val sharedKafkaTestResource = SharedKafkaTestResource()
        val kafka: KafkaTestUtils by lazy{ sharedKafkaTestResource.kafkaTestUtils }
    }
}