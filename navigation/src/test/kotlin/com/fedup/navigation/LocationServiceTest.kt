package com.fedup.navigation

import com.salesforce.kafka.test.junit4.*
import org.junit.*
import org.junit.runner.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.context.*
import org.springframework.test.context.junit4.*


@RunWith(SpringRunner::class)
@SpringBootTest
class LocationServiceTest {
    @Autowired private lateinit var locationService: LocationService

    @Before
    fun setUp() {
        locationService.processStreams()
    }

    @Test
    fun `dont know what`() {
        println("-----------------------------------------------------------------------------------------")
        println("-----------------------------------------------------------------------------------------")
        println("-----------------------------------------------------------------------------------------")
        sharedKafkaTestResource.kafkaTestUtils.topicNames.forEach { println("Topic: $it") }
        println("-----------------------------------------------------------------------------------------")
    }

    companion object {
        @ClassRule @JvmField public val sharedKafkaTestResource = SharedKafkaTestResource()
    }
}