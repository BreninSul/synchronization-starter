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
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class represents a client lock.
 *
 * @property usedAt is the time when the client lock was created. Default value is the current time.
 *
 * @constructor Initialize the client lock with the time of creation.
 *
 **/
open class ClientLock(@Volatile var usedAt: LocalDateTime = LocalDateTime.now(),) {
    val isTimeout: AtomicBoolean = AtomicBoolean(false)
    /**
     * Check if this client lock is equal to another object.
     *
     * @param other is the object to be compared with this client lock.
     * @return a boolean. Returns true if the two are equal. Otherwise, it returns false.
     **/
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientLock

        return usedAt == other.usedAt
    }

    /**
     * Get the hashcode of this client lock.
     *
     * @return an int value which is the hashcode of this client lock.
     **/
    override fun hashCode(): Int {
        return usedAt.hashCode()
    }

    /**
     * Get the string representation of this client lock.
     *
     * @return a string representation of this client lock.
     **/
    override fun toString(): String {
        return "ClientLock(usedAt=$usedAt)"
    }
}
