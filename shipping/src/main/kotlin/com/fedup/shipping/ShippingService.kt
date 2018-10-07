package com.fedup.shipping

import com.fedup.common.*
import org.apache.kafka.common.serialization.*
import org.springframework.stereotype.*

/**
 * Subscribes to shipments stream, where it listens for certain events that require action, like to publish
 * some other events to other topics, etc.
 *
 * TODO create the topics it writes to (single-writer) and tables it uses; define its topics here instead of common
 */
@Service
class ShippingService {
    fun start() {
    }

    /**
     * Following the single-writer principle, defining all the topics to which this service is the sole writer
     * here.
     */
    object OwnedTopics {
        val shipments = Topic("shipments", Serdes.String(), Serdes.String())
        val userNotification = Topic("user-notification", Serdes.String(), Serdes.String())
    }
}