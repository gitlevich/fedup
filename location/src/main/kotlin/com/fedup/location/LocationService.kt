package com.fedup.location

import com.fedup.shared.*
import com.fedup.shared.machinery.*
import com.fedup.shared.protocol.*
import com.fedup.shared.protocol.Topics.availableDrivers
import com.fedup.shared.protocol.Topics.driverRequests
import com.fedup.shared.protocol.Topics.userLocations
import com.fedup.shared.protocol.location.*
import org.apache.kafka.clients.producer.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.kstream.*
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
    private val mapService: MapsIntegrationService,
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
     * Locates all active drivers, where "active" is a driver that has reported his location within last hour
     *
     * Note: this is not an ideal implementation, although it can be mitigated by windowing [userLocationStore]
     * to our agreed upon activity window, currently hardcoded as 1 hour, as it loads all drivers. A better
     * implementation would use a graph algorithm to restrict the driver search space to a geographic area,
     * e.g. a bounding rectangle, with the shipper in its middle.
     */
    private fun findActiveDrivers(): List<UserLocation> =
        try {
            streams.store(userLocationStore, QueryableStoreTypes.keyValueStore<UserId, UserLocation>())
                .all().iterator().asSequence()
                .map { (it.value) }
                .filter { userLocation -> userLocation.coordinates.time.isAfter(OffsetDateTime.now().minusHours(1)) }
                .toList()
        } catch (e: Exception) {
            // FIXME This is actually a lie: we don't know if drivers are available. Work out what to do here.
            throw NoDriversAvailable(e)
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
        KafkaStreams(locationServiceTopology(mapService) { findActiveDrivers() }.build(), kafkaConfig.props).also {
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
        private const val userLocationStore = "user_locations_store"

        /**
         * Defines stream processing topology for Location Service. Given this is just a stateless and side effect free
         * definition of the pipeline, I deliberately placed it into companion object to underscore that fact.
         */
        fun locationServiceTopology(mapService: MapsIntegrationService, findDrivers: () -> List<UserLocation>): StreamsBuilder {
            val topology = StreamsBuilder()

            // put user locations into userLocationStore where we can find them later
            topology
                .stream<String, UserLocation>(
                    userLocations.name,
                    Consumed.with(userLocations.keySerde, userLocations.valueSerde)
                )
                .filter { _, userLocation -> userLocation.userRole == UserRole.DRIVER }
                .groupByKey()
                .reduce(
                    { ul1, ul2 -> if (ul1.coordinates.time.isAfter(ul2.coordinates.time)) ul1 else ul2 },
                    Materialized.`as`(userLocationStore)
                )

            val driverRequests = topology.stream(
                driverRequests.name,
                Consumed.with(driverRequests.keySerde, driverRequests.valueSerde)
            )

            // transform driver request stream to a stream of available drivers
            driverRequests
                .mapValues { request ->
                    DriversLocated(
                        request.trackingId,
                        mapService.findNearestUsers(request.location, findDrivers)
                    )
                }
                .to(
                    availableDrivers.name,
                    Produced.with(availableDrivers.keySerde, availableDrivers.valueSerde)
                )

            return topology
        }
    }
}

class UserNotFound(userId: UserId) : Exception("User $userId not found")
class NoDriversAvailable(cause: Throwable) : Exception("No drivers are available", cause)
