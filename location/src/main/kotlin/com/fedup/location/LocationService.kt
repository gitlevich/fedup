package com.fedup.location

import com.fedup.shared.machinery.*
import com.fedup.shared.protocol.*
import com.fedup.shared.protocol.Topics.availableDrivers
import com.fedup.shared.protocol.Topics.driverRequests
import com.fedup.shared.protocol.Topics.userLocations
import com.fedup.shared.protocol.location.*
import com.fedup.shared.protocol.shipment.*
import org.apache.kafka.clients.producer.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.Topology.AutoOffsetReset.*
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.processor.*
import org.apache.kafka.streams.state.*
import org.slf4j.*
import org.springframework.stereotype.*
import java.time.*

/**
 * Responsible for tracking user locations and
 *
 * Specifically:
 * - it records locations reported by users to [userLocations] stream
 * - listens for [NearbyDriversRequested] events on location-requests topic, finds the drivers closest to the specified
 *   location and publishes [DriversLocated] events to available-drivers
 *
 * Owns (is a single writer to) both [userLocations] and [availableDrivers] streams.
 * Consumes from [driverRequests] topic to know when to look for drivers
 */
@Component
class LocationService(
    private val topology: StreamsBuilder,
    private val kafkaConfig: KafkaStreamsConfig
) : KafkaService {

    /**
     * Writes to the [userLocations] stream, owned by this service, the current location of the user. Since we
     * probably are not interested in tracking user's trajectory over a long time, the underlying topic can be
     * aggressively compacted, e.g. have a retention policy of about an hour (because older locations are of little
     * use to our scenario)
     */
    fun recordUserLocation(userLocation: UserLocation) {
        userLocationsProducer.send(ProducerRecord(Topics.userLocations.name, userLocation.userId, userLocation))
    }

    /**
     * Looks up the user in a KTable event-sourced from [userLocations] stream
     */
    fun locateUser(userId: UserId): UserLocation? =
        streams.store(userLocationStore, QueryableStoreTypes.keyValueStore<UserId, UserLocation>())[userId]

    override fun close() {
        streams.close()
    }

    /**
     * An instance of KafkaStreams that expresses our processing topology
     */
    private val streams by lazy {
        KafkaStreams(topology.build(), kafkaConfig.props)
            .also {
                it.start()
                logger.info("Started location service")
            }
    }

    /**
     * The producer instance we use to write user locations to the topic this service owns.
     */
    private val userLocationsProducer by lazy {
        KafkaProducer(
            kafkaConfig.props,
            Topics.userLocations.keySerde.serializer(),
            Topics.userLocations.valueSerde.serializer()
        )
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(LocationService::class.java)
        internal const val userLocationStore = "user_locations_store"

        /**
         * Defines stream processing topology for Location Service. Given this is just a stateless and side effect free
         * definition of the pipeline, I deliberately placed it into companion object to underscore that fact.
         */
        fun topology(mapService: MapsIntegrationService): StreamsBuilder =
            StreamsBuilder().apply {
                table<String, UserLocation>(
                        Topics.userLocations.name,
                        Consumed
                            .with(userLocations.keySerde, userLocations.valueSerde)
                            .withOffsetResetPolicy(EARLIEST),
                        Materialized.`as`(userLocationStore)
                    )
                    .toStream()
                    .peek { key, value -> println("### locations: $key: $value") }
                    .to("trash", Produced.with(userLocations.keySerde, userLocations.valueSerde))

                stream(driverRequests.name, Consumed.with(driverRequests.keySerde, driverRequests.valueSerde))
                    .peek { key, value -> println("### request: $key: $value") }
                    .transformValues(
                        ValueTransformerSupplier { DriverRequestTransformer(mapService, userLocationStore) },
                        userLocationStore
                    )
                    .peek { key, value -> println("### response: $key: $value") }
                    .to(availableDrivers.name, Produced.with(availableDrivers.keySerde, availableDrivers.valueSerde))
            }
    }
}

class UserNotFound(userId: UserId) : Exception("User $userId not found")
class NoDriversAvailable(cause: Throwable) : Exception("No drivers are available", cause)

class DriverRequestTransformer(
    private val mapService: MapsIntegrationService,
    private val storeName: String
) : ValueTransformer<NearbyDriversRequested, DriversLocated> {

    private lateinit var userLocationStore: KeyValueStore<UserId, UserLocation>
    private lateinit var processorContext: ProcessorContext

    override fun init(context: ProcessorContext) {
        processorContext = context
        userLocationStore = processorContext.getStateStore(storeName) as KeyValueStore<UserId, UserLocation>
    }

    override fun transform(request: NearbyDriversRequested): DriversLocated {
        val allUserLocations = userLocationStore.all().iterator().asSequence().map { it.value }
            .filter { userLocation -> isRecentEnough(userLocation) }
            .toList()

        return DriversLocated(
            request.trackingId,
            mapService.findNearestUsers(request.location, allUserLocations)
        )
    }

    private fun isRecentEnough(userLocation: UserLocation) =
        userLocation.coordinates.time.isAfter(OffsetDateTime.now().minusHours(1))

    override fun close() {}
}