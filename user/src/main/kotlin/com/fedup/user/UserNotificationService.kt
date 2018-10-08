package com.fedup.user

import com.fedup.common.*
import org.apache.kafka.common.serialization.*

/**
 * Subscribes to user-notifications stream and pushes events from this stream to the users they are addressed to
 */
class UserNotificationService {

    /**
     * Pushes the specified notification to the user's device
     */
    fun notifyUser(user: User, notification: Any) {

    }
}