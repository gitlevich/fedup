package com.fedup.location

import com.fedup.shared.machinery.KafkaStreamsConfig
import com.fedup.shared.machinery.readOne
import com.fedup.shared.machinery.send
import com.fedup.shared.machinery.sendOne
import com.fedup.shared.protocol.Topics
import com.fedup.shared.protocol.location.Location
import com.fedup.shared.protocol.location.STC
import com.fedup.shared.protocol.location.UserLocation
import com.fedup.shared.protocol.location.UserRole
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class LocationServiceIT {

    @Test
    fun `should write NearbyDriversRequested to driverRequests topic`() {
        val driverRequest = LocationEventGenerator.generateDriverRequests(howMany = 1).first()
        sendOne(
            ProducerRecord(Topics.driverRequests.name, driverRequest.trackingId, driverRequest),
            Topics.driverRequests
        )

        val readMessages = readOne(Topics.driverRequests, kafkaConfig.bootstrapServers)

        assertThat(readMessages)
            .isNotEmpty
            .anyMatch { it.key == driverRequest.trackingId }    }

    @Test
    fun `should write DriversLocated to availableDrivers topic`() {
        val availableDrivers = LocationEventGenerator.generateDriversLocatedEvents(howMany = 1).first()
        sendOne(
            ProducerRecord(Topics.availableDrivers.name, availableDrivers.trackingId, availableDrivers),
            Topics.availableDrivers
        )

        val readMessages = readOne(Topics.availableDrivers, kafkaConfig.bootstrapServers)

        assertThat(readMessages)
            .isNotEmpty
            .anyMatch { it.key == availableDrivers.trackingId }    }

    @Test
    fun `should publish DriversLocated event to available-drivers topic upon receiving NearbyDriversRequested`() {
        val driverRequest = LocationEventGenerator.generateDriverRequests(howMany = 1).first()
        sendOne(
            ProducerRecord(Topics.driverRequests.name, driverRequest.trackingId, driverRequest),
            Topics.driverRequests
        )

        val availableDriversEvents = readOne(Topics.availableDrivers, kafkaConfig.bootstrapServers)

        assertThat(availableDriversEvents)
            .isNotEmpty
            .anyMatch { it.key == driverRequest.trackingId }
    }


//    @Ignore("I have not been able to get Kafka state store to work for me yet")
    @Test
    fun `should find stored user location`() {
        val original = UserLocation("driver@drivers.com", Location(37.7534327, -122.4344288), UserRole.DRIVER)
        locationService.recordUserLocation(original)

        val retrieved = locationService.locateUser(original.userId)
        assertThat(retrieved).isEqualTo(original)
    }

    /* * * * * * * * * * * * * * * * * *   M A C H I N E R Y   * * * * * * * * * * * * * * * * */
    @Autowired private lateinit var locationService: LocationService
    @Autowired private lateinit var kafkaConfig: KafkaStreamsConfig

    @Before
    fun setUp() {
        val driverLocations = LocationEventGenerator.generateDriverLocations(1)
        send(
            driverLocations.map { event -> ProducerRecord(Topics.userLocations.name, event.userId, event) },
            Topics.userLocations
        )
    }

    @After
    fun tearDown() {
        locationService.close()
    }
}