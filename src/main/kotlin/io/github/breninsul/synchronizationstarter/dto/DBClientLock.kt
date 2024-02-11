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

import java.sql.Statement
import java.time.LocalDateTime

/**
 * This class represents a client lock for a database client. It extends the ClientLock class.
 *
 * @property statement The database statement associated with the lock.
 * @property usedAt The timestamp when the lock was created. Default value is the current time.
 *
 * @constructor Initializes a DBClientLock instance with the provided database statement and creation timestamp.
 *
 */
class DBClientLock(
    val statement: Statement,
    createdAt: LocalDateTime = LocalDateTime.now(),
) : ClientLock(createdAt) {
    /**
     * Checks if this object is equal to another.
     *
     * @param other The object to check for equality.
     * @return True if the objects are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as DBClientLock

        return statement == other.statement
    }

    /**
     * Generates a hash code for the object.
     *
     * @return The generated hash code.
     */
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + statement.hashCode()
        return result
    }
    /**
     * Returns a string representation of the object.
     *
     * @return A string that contains the statement and the creation time of the lock.
     */
    override fun toString(): String {
        return "DBClientLock(statement=$statement,usedAt=${usedAt})"
    }

}
