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

package com.github.breninsul.synchronizationstarter.service.zookeeper

import com.github.breninsul.synchronizationstarter.dto.ZookeeperClientLock
import com.github.breninsul.synchronizationstarter.service.clear.ClearableSynchronisationService
import com.github.breninsul.synchronizationstarter.service.longHash
import org.apache.zookeeper.*
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Level
import java.util.logging.Logger


open class ZookeeperSynchronizationService(protected val zooKeeper: ZooKeeper, protected val maxLifetime: Duration) : ClearableSynchronisationService<ZookeeperClientLock> {
    protected open val logger: Logger = Logger.getLogger(this.javaClass.name)
    protected open val internalLock = ReentrantLock()
    protected open val locks: ConcurrentMap<Any, ZookeeperClientLock> = ConcurrentHashMap()
    override fun getAllLocksAfter(lifetime: Duration): List<Pair<Any, ZookeeperClientLock>> {
        TODO("Not yet implemented")
    }

    override fun clear(id: Any) {
        TODO("Not yet implemented")
    }

    override fun unlockTimeOuted(id: Any, lifetime: Duration) {
        TODO("Not yet implemented")
    }

    override fun before(id: Any): Boolean {
        return before(id,false)
    }
    protected open fun before(id: Any,completeAnswer:Boolean ): Boolean {
        val lockId = "lock_${id.longHash()}"
        val lock = getLock(id)
        val futureToWait = CompletableFuture<Boolean>()
        if (maxLifetime.toMillis() > 0) {
            futureToWait.orTimeout(maxLifetime.toMillis(), TimeUnit.MILLISECONDS);
        }
        try {
            zooKeeper.create(lockId, byteArrayOf(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
            futureToWait.complete(completeAnswer)
        } catch (e: KeeperException.NodeExistsException) {
            val state = zooKeeper.exists(lockId, object : Watcher {
                override fun process(event: WatchedEvent) {
                    if (event.type == Watcher.Event.EventType.NodeDeleted) {
                        futureToWait.complete(true)
                    } else {
                        try {
                            val state = zooKeeper.exists(lockId, this)
                            if (state == null) {
                                futureToWait.complete(before(id,true))
                            }
                        } catch (t: Throwable) {
                            logger.log(Level.SEVERE, "Error reset watcher to zookeeper ${lockId}", t)
                        }
                    }
                }
            })
            if (state == null) {
                futureToWait.complete(completeAnswer)
            }
        }
        try {
            return futureToWait.get()
        } catch (t:TimeoutException){
            unlock(id,lock)
            return true
        }
    }

    override fun after(id: Any) {
        val clientLock = locks[id]
        if (clientLock == null) {
            logger.log(Level.SEVERE, "No lock for  $id")
        } else {
            logger.log(Level.FINEST, "Lock for $id released")
            unlock(id, clientLock)
        }
    }


    protected open fun getLock(id: Any): ZookeeperClientLock {
        internalLock.lock()
        try {
            val clientLock = locks[id]
            return if (clientLock == null) {
                val lock = ZookeeperClientLock()
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
     * Unlocks a client lock.
     * @param id Lock id
     * @param l The client lock to unlock.
     */
    protected open fun unlock(id: Any, l: ZookeeperClientLock) {
        val lockId = "lock_${id.longHash()}"
        internalLock.lock()
        try {
            zooKeeper.delete(lockId, -1)
            l.createdAt = LocalDateTime.now()
        } catch (t: Throwable) {
            logger.log(Level.WARNING, "Error unlocking lock ! ${t.javaClass}:${t.message}", t)
        } finally {
            internalLock.unlock()
        }
    }
}