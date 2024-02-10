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

package com.github.breninsul.synchronizationstarter.service.local

import com.github.breninsul.synchronizationstarter.dto.LocalClientLock
import com.github.breninsul.synchronizationstarter.service.clear.ClearableSynchronisationService
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.StampedLock
import java.util.logging.Level
import java.util.logging.Logger

open class LocalSynchronizationService(protected val normalLockTime: Duration) : ClearableSynchronisationService<LocalClientLock> {
    protected open val logger: Logger = Logger.getLogger(this.javaClass.name)
    protected open val internalLock = ReentrantLock()
    protected open val locks: ConcurrentMap<Any, LocalClientLock> = ConcurrentHashMap()

    override fun getAllLocksAfter(lifetime: Duration): List<Pair<Any, LocalClientLock>> {
        val clearBefore = LocalDateTime.now().minus(lifetime)
        return locks.filter { it.value.createdAt.isBefore(clearBefore) }.map { it.key to it.value }
    }

    override fun clear(id: Any) {
        internalLock.lock()
        try {
            val clientLock = locks[id]
            if (clientLock?.lock?.isWriteLocked == false) {
                locks.remove(id)
                if (clientLock.lock.isWriteLocked) {
                    locks[id] = clientLock
                    logger.log(Level.SEVERE, "Lock for $id was cleared but became locked after deletion!")
                }
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
        logger.log(Level.FINEST, "Lock for $id set")
        val time = System.currentTimeMillis()
        val lock = getLock(id)
        val locked: Boolean = lock.lock.isWriteLocked
        val stamp: Long = lock.lock.writeLock()
        lock.stamp = (stamp)
        val tookMs = System.currentTimeMillis() - time
        logger.log(Level.FINEST, "Lock $id is locked : $locked .Lock Took $tookMs ms")
        if (tookMs > normalLockTime.toMillis()) {
            logger.log(Level.SEVERE, "Lock took more then ${normalLockTime.toMillis()}ms $tookMs $id")
        }
        return locked
    }

    /**
     * Retrieves a lock based on its id.
     * @param id The id of the lock.
     * @return The lock associated with the given id.
     */
    protected open fun getLock(id: Any,): LocalClientLock {
        internalLock.lock()
        try {
            val clientLock = locks[id]
            return if (clientLock == null) {
                val lock = LocalClientLock()
                locks[id] = lock
                lock
            } else {
                clientLock
            }
        } finally {
            internalLock.unlock()
        }
    }

    /**
     * Performs the after method.
     * @param id The id of the lock.
     */
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
    protected open fun unlock(l: LocalClientLock) {
        internalLock.lock()
        try {
            val lock: StampedLock = l.lock
            lock.unlockWrite(l.stamp!!)
            l.createdAt= LocalDateTime.now()
        } catch (t: Throwable) {
            logger.log(Level.WARNING, "Error unlocking lock ! ${t.javaClass}:${t.message}", t)
        } finally {
            internalLock.unlock()
        }
    }
}
