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

package io.github.breninsul.synchronizationstarter.dto

import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class represents a lock which is associated with a Zookeeper client.
 * It extends the `ClientLock` class and includes a `CompletableFuture` to manage asynchronous operations.
 *
 * @property usedAt The time at which the lock was created. By default, it's the time of object instantiation.
 */
class ZookeeperClientLock(
    createdAt: LocalDateTime = LocalDateTime.now(),
) : ClientLock(createdAt) {

    /**
     * Overridden equals method.
     *
     * @param other the object to compare with this instance.
     * @return true if the objects are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

    /**
     * Overridden hashCode method.
     *
     * @return a hash code value for the object.
     */
    override fun hashCode(): Int {
        var result = super.hashCode()
        return result
    }

    /**
     * Overridden toString method.
     *
     * @return a string representation of the object.
     */
    override fun toString(): String {
        return "ZookeeperClientLock(usedAt=${usedAt})"
    }

}
