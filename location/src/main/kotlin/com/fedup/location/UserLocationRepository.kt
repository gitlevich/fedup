package com.fedup.location

import com.fedup.shared.*
import com.fedup.shared.machinery.*
import com.fedup.shared.protocol.Topics.userLocations
import com.fedup.shared.protocol.location.*
import org.apache.kafka.clients.producer.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.*
import org.slf4j.*
import org.springframework.stereotype.*


/**
 * Sits on top of user-location stream
 */
@Component
class UserLocationRepository(private val kafkaConfig: KafkaStreamsConfig) : KafkaService, AutoCloseable {
    private val userLocationStore = "user_locations_store"
    private val streams by lazy { buildUserLocationsStream() }
    private val userLocationsProducer by lazy {
        KafkaProducer(
            kafkaConfig.props,
            userLocations.keySerde.serializer(),
            userLocations.valueSerde.serializer()
        )
    }

    fun buildUserLocationsStream(): KafkaStreams {
        val builder = StreamsBuilder()

        builder.stream<String, UserLocation>(
            userLocations.name,
            Consumed.with(userLocations.keySerde, userLocations.valueSerde)
        ).groupByKey()
        .reduce(
            {value1, value2 ->  value1},
            Materialized.`as`(userLocationStore)
        )

        return KafkaStreams(builder.build(), kafkaConfig.props)
    }

    fun findAllDrivers(): List<UserLocation> =
        try {
            streams.store(userLocationStore, QueryableStoreTypes.keyValueStore<UserId, UserLocation>())
                .all().iterator().asSequence()
                .map { (it.value) }
                .toList()
        } catch (e: Exception) {
            logger.error("Unable to find drivers", e)
            emptyList()
        }

    fun findLocationFor(userId: UserId): UserLocation? =
        streams.store(userLocationStore, QueryableStoreTypes.keyValueStore<UserId, UserLocation>())[userId]


    fun saveUserLocation(userLocation: UserLocation) {
        userLocationsProducer.send(ProducerRecord(userLocations.name, userLocation.userId, userLocation))
    }

    override fun start() {
        logger.info("Starting streams")
        streams.start()
    }

    override fun stop() {
        streams.close()
        logger.info("Streams closed")
    }

    override fun close() {
        stop()
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(UserLocationRepository::class.java)
    }

}

class UserNotFoundException(userId: UserId) : Exception("User $userId not found")