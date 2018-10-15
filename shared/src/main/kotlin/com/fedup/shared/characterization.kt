package com.fedup.shared

/**
 * DDD Entity pattern.
 *
 * This class captures the main characteristic of an entity: any two instances with the same identity
 * represent the same entity.
 */
abstract class Entity<ID> {
    abstract val identity: ID

    final override fun hashCode(): Int = identity?.hashCode() ?: 0

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity<*>) return false

        if (identity != other.identity) return false

        return true
    }
}

/**
 * ValueObject DDD pattern. I added it here for completeness only, as a marker interface: Kotlin has data classes,
 * which is a native structure to represent values. If someone feels like marking every value, this is the thing
 * to use (and good luck!)
 */
interface ValueObject