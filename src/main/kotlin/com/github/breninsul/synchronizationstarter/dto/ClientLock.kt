package com.github.breninsul.synchronizationsatrter.dto

import java.time.LocalDateTime
import java.util.concurrent.locks.StampedLock

open class ClientLock(
    val lock: StampedLock = StampedLock(),
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    @Volatile
    var stamp: Long? = null
    override fun toString(): String {
        return "ClientLock(lock=$lock, createdAt=$createdAt, stamp=$stamp)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientLock

        if (lock != other.lock) return false
        if (createdAt != other.createdAt) return false
        if (stamp != other.stamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lock.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (stamp?.hashCode() ?: 0)
        return result
    }

}