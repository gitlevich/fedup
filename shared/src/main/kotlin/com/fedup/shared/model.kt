package com.fedup.shared

typealias UserId = String

object Characterization {
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
}


