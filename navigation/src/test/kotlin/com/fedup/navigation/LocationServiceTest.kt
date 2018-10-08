package com.fedup.navigation

import com.fedup.common.*
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
        println("-----------------------------------------------------------------------------------------")
        println("-----------------------------------------------------------------------------------------")
        println("-----------------------------------------------------------------------------------------")
        sharedKafkaTestResource.kafkaTestUtils.topicNames.forEach { println("Topic: $it") }
        println("-----------------------------------------------------------------------------------------")

        val request = NearbyDriversRequested(TrackingId("123"), Location(37.7724868, -122.4166086))
        val locationRequests = mapOf(
            objectMapper.writeValueAsBytes(request.trackingId) to objectMapper.writeValueAsBytes(request)
        )

        kafka.produceRecords(locationRequests, locationService.locationRequests.name, 0)
        val records = kafka.consumeAllRecordsFromTopic(locationService.availableDrivers.name)
            .map { objectMapper.readValue(it.key(), TrackingId::class.java) to objectMapper.readValue(it.key(), DriversLocated::class.java) }

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