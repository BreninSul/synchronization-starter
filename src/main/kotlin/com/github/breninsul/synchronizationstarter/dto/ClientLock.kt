/*
 * MIT License
 *
 * Copyright (c) 2024 BreninSul
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.breninsul.synchronizationstarter.dto

import java.time.LocalDateTime
import java.util.concurrent.locks.StampedLock

/**
 * This class represents a lock mechanism for clients.
 * It encapsulates StampedLock instance with the creation timestamp and a stamp that may be used for operation status tracking.
 *
 * @property lock StampedLock instance.
 * @property createdAt LocalDateTime instance representing creation timestamp of the lock.
 * @property stamp Long value representing a stamp for lock operations that might be null.
 */
open class ClientLock(
    val lock: StampedLock = StampedLock(),
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    @Volatile
    var stamp: Long? = null

    /**
     * Provides a string representation of the ClientLock object.
     * @return Returns a string representing the current instance of ClientLock object.
     */
    override fun toString(): String {
        return "ClientLock(lock=$lock, createdAt=$createdAt, stamp=$stamp)"
    }

    /**
     * Determines whether the specified object is equal to the current instance.
     * @param other The other object to be compared with current instance.
     * @return Returns true if the specified object is equal to the current instance; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientLock

        if (lock != other.lock) return false
        if (createdAt != other.createdAt) return false
        if (stamp != other.stamp) return false

        return true
    }

    /**
     * Compute a hash code for the current instance.
     * @return Returns a hash code generated for the current instance.
     */
    override fun hashCode(): Int {
        var result = lock.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (stamp?.hashCode() ?: 0)
        return result
    }
}