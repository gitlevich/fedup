package com.fedup.location

import com.fedup.shared.*
import com.fedup.shared.machinery.*
import com.fedup.shared.protocol.*
import org.apache.kafka.clients.producer.*
import org.assertj.core.api.Assertions.*
import org.junit.*
import org.junit.runner.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.context.*
import org.springframework.test.context.junit4.*

@RunWith(SpringRunner::class)
@SpringBootTest
class LocationServiceTest {

    private val trackingId = TrackingId("444-433-222")

    @Test
    fun `should publish DriversLocated event to available-drivers topic upon receiving NearbyDriversRequested`() {
        val driverRequests = LocationEventGenerator.generateDriverRequests(trackingId, 1)
        send(
            driverRequests.map { event -> ProducerRecord(Topics.locationRequests.name, event.trackingId, event) },
            Topics.locationRequests
        )

        val availableDriversEvents = readOne(Topics.availableDrivers, kafkaConfig.bootstrapServers)

        assertThat(availableDriversEvents)
            .isNotEmpty
            .anyMatch { it.key == trackingId }
    }

    /* * * * * * * * * * * * * * * * * *   M A C H I N E R Y   * * * * * * * * * * * * * * * * */
    @Autowired private lateinit var locationService: LocationService
    @Autowired private lateinit var kafkaConfig: KafkaStreamsConfig


    @After
    fun tearDown() {
        locationService.stop()
    }
}