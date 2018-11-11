package com.fedup.location

import com.fedup.shared.machinery.*
import com.fedup.shared.protocol.Topics.availableDrivers
import com.fedup.shared.protocol.Topics.driverRequests
import com.fedup.shared.protocol.Topics.userLocations
import com.fedup.shared.protocol.location.*
import com.fedup.shared.protocol.shipment.UserId
import org.apache.kafka.clients.producer.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.Topology.AutoOffsetReset.EARLIEST
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.state.*
import org.slf4j.*
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

/**
 * Responsible for tracking user locations and finding the users closest to others.
 *
 *
 * Thoughts: actually, we want to figure out the partition to which to write the user location based on
 * location, not user id (although we might need that as well to answer the question "where's the user?")
 *
 * So the trick here is to break our map of the city into squares and create a hash function that maps a location
 * to one of these squares. Then, to answer the question "who are the nearest driver to this location?" we first
 * determine calculate the squares included in a circle with the specified radius with the center in the location
 * in question, then look up all the drivers in the found squares, and then, as the last step, send the found
 * drivers location to Google Map API to pick the few closest (by driving time) to the given location. Which
 * means that we drop the hard dependency on Google Maps availability and can continue with somewhat degraded, but still
 * useful, service even when the Google API that we don't control becomes unavailable.
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
    topology: Topology,
    kafkaConfig: KafkaStreamsConfig
) : KafkaService {

    /**
     * Writes to the [userLocations] stream, owned by this service, the current location of the user. Since we
     * probably are not interested in tracking user's trajectory over a long time, the underlying topic can be
     * aggressively compacted, e.g. have a retention policy of about an hour (because older locations are of little
     * use to our scenario)
     *
     * TODO: add writing to a topic with the key being location hash (where locations in the same square have the
     * same key)
     */
    fun recordUserLocation(userLocation: UserLocation) {
        userLocationsProducer.send(ProducerRecord(userLocations.name, userLocation.userId, userLocation))
    }

    /**
     * Looks up the user in a store associated with a GlobalKTable event-sourced from [userLocations] stream
     */
    fun locateUser(userId: UserId): UserLocation? =
        streams.store(userLocationStore, QueryableStoreTypes.keyValueStore<UserId, UserLocation>())[userId]

    override fun close() {
        streams.close()
    }

    /**
     * An instance of KafkaStreams with our processing topology
     */
    private val streams: KafkaStreams =
        KafkaStreams(topology, kafkaConfig.props)
            .also {
                it.setUncaughtExceptionHandler { _, e ->
                    e.printStackTrace()
                }
                it.start()
                logger.info("Started location service")
            }

    /**
     * The producer instance we use to write user locations to the topic this service owns.
     */
    private val userLocationsProducer = KafkaProducer(
        kafkaConfig.props,
        userLocations.keySerde.serializer(),
        userLocations.valueSerde.serializer()
    )

    companion object {
        val logger: Logger = LoggerFactory.getLogger(LocationService::class.java)
        internal const val userLocationStore = "user_locations_store"

        /**
         * Defines stream processing topology for Location Service. Given this is just a stateless and side effect free
         * definition of the pipeline, I deliberately placed it into companion object to underscore that fact.
         */
        fun topology(mapService: MapsIntegrationService): Topology =
            StreamsBuilder()
                .apply {
                    globalTable<String, UserLocation>(
                        userLocations.name,
                        Consumed
                            .with(userLocations.keySerde, userLocations.valueSerde)
                            .withOffsetResetPolicy(EARLIEST),
                        Materialized.`as`(userLocationStore)
                    )

                    stream(driverRequests.name, Consumed.with(driverRequests.keySerde, driverRequests.valueSerde))
                        .peek { key, value -> println("### request: $key: $value") }
                        .transformValues(
                            ValueTransformerSupplier { DriverRequestTransformer(mapService, userLocationStore) }
                        )
                        .peek { key, value -> println("### response: $key: $value") }
                        .to(availableDrivers.name, Produced.with(availableDrivers.keySerde, availableDrivers.valueSerde))
                }
                .build()
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