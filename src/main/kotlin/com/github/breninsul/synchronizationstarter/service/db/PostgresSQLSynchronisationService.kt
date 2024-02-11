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

package com.github.breninsul.synchronizationstarter.service.db

import com.github.breninsul.synchronizationstarter.dto.DBClientLock
import com.github.breninsul.synchronizationstarter.service.clear.ClearableSynchronisationService
import com.github.breninsul.synchronizationstarter.service.longHash
import java.sql.Statement
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Level
import java.util.logging.Logger
import javax.sql.DataSource

open class PostgresSQLSynchronisationService(protected val dataSource: DataSource, protected val normalLockTime: Duration) : ClearableSynchronisationService<DBClientLock> {
    protected open val logger: Logger = Logger.getLogger(this.javaClass.name)
    protected open val internalLock = ReentrantLock()
    protected open val locks: ConcurrentMap<Any, DBClientLock> = ConcurrentHashMap()

    override fun getAllLocksAfter(lifetime: Duration): List<Pair<Any, DBClientLock>> {
        val clearBefore = LocalDateTime.now().minus(lifetime)
        return locks.filter { it.value.createdAt.isBefore(clearBefore) }.map { it.key to it.value }
    }

    override fun clear(id: Any) {
        internalLock.lock()
        try {
            val clientLock = locks[id]
            if (clientLock?.statement?.connection?.isClosed != false) {
                locks.remove(id)
            }
        } finally {
            internalLock.unlock()
        }
    }

    override fun unlockTimeOuted(
        id: Any,
        lifetime: Duration,
    ) {
        val clientLock = locks[id]
        if (clientLock?.createdAt?.isBefore(LocalDateTime.now().minus(lifetime)) == true) {
            unlock(clientLock)
        } else {
            logger.log(Level.FINEST, "Lock is not timed out!")
        }
    }

    override fun before(id: Any): Boolean {
        val connection = dataSource.connection
        try {
            logger.log(Level.FINEST, "Lock for $id set")
            val time = System.currentTimeMillis()
            connection.autoCommit = false
            val statement = connection.createStatement()
            statement.execute("BEGIN;")
            val rs =
                statement.executeQuery(
                    """
                SELECT case when pg_try_advisory_xact_lock(${id.longHash()}) then false else 
                (select true from pg_advisory_xact_lock(${id.longHash()})) end 
                    """.trimMargin(),
                )
            rs.next()
            val hadLock = rs.getBoolean(1)
            val tookMs = System.currentTimeMillis() - time
            logger.log(Level.FINEST, "Lock $id is locked : $hadLock .Lock Took $tookMs ms")
            if (tookMs > normalLockTime.toMillis()) {
                logger.log(Level.SEVERE, "Lock took more then ${normalLockTime.toMillis()}ms $tookMs $id")
            }
            getLock(id, statement)
            return hadLock
        } catch (t: Throwable) {
            logger.log(Level.SEVERE, "Error using connection before sync", t)
            connection.close()
            throw t
        }
    }

    override fun after(id: Any) {
        val clientLock = locks[id]
        if (clientLock == null) {
            logger.log(Level.SEVERE, "No lock for  $id")
        } else {
            logger.log(Level.FINEST, "Lock for $id released")
            unlock(clientLock)
        }
    }

    /**
     * Unlocks a client lock.
     * @param l The client lock to unlock.
     */
    protected open fun unlock(l: DBClientLock) {
        internalLock.lock()
        try {
            val statement = l.statement
            statement.execute("COMMIT;")
        } catch (t: Throwable) {
            logger.log(Level.WARNING, "Error committing lock ! ${t.javaClass}:${t.message}", t)
        } finally {
            try {
                if (!l.statement.connection.isClosed) {
                    l.statement.connection.close()
                }
            } catch (t: Throwable) {
                logger.log(Level.WARNING, "Error closing connection! ! ${t.javaClass}:${t.message}", t)
            }
            internalLock.unlock()
        }
    }

    /**
     * Performs the after method.
     * @param id The id of the lock.
     * @param statement DB connection statement
     */
    protected open fun getLock(
        id: Any,
        statement: Statement,
    ): DBClientLock {
        internalLock.lock()
        try {
            val lock = DBClientLock(statement)
            locks[id] = lock
            return lock
        } finally {
            internalLock.unlock()
        }
    }
}
