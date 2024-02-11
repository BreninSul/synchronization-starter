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

package io.github.breninsul.synchronizationstarter.service.zookeeper

import io.github.breninsul.synchronizationstarter.dto.ZookeeperClientLock
import io.github.breninsul.synchronizationstarter.exception.SyncTimeoutException
import io.github.breninsul.synchronizationstarter.service.clear.ClearableSynchronisationService
import io.github.breninsul.synchronizationstarter.service.longHash
import org.apache.zookeeper.*
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Level
import java.util.logging.Logger


open class ZookeeperSynchronizationService(protected val zooKeeper: ZooKeeper, protected val normalLockTime: Duration, protected val maxLifetime: Duration,val pathPrefix:String) : ClearableSynchronisationService<ZookeeperClientLock> {
    protected open val logger: Logger = Logger.getLogger(this.javaClass.name)
    protected open val internalLock = ReentrantLock()
    protected open val locks: ConcurrentMap<Any, ZookeeperClientLock> = ConcurrentHashMap()
    override fun getAllLocksAfter(lifetime: Duration): List<Pair<Any, ZookeeperClientLock>> {
        val clearBefore = LocalDateTime.now().minus(lifetime)
        return locks.filter { it.value.usedAt.isBefore(clearBefore) }.map { it.key to it.value }
    }

    override fun clear(id: Any) {
        internalLock.lock()
        try {
            val clientLock = getLock(id)
            locks.remove(id)
        } finally {
            internalLock.unlock()
        }
    }

    override fun unlockTimeOuted(id: Any, lifetime: Duration) {
        val clientLock = getLock(id)
        if (clientLock?.usedAt?.isBefore(LocalDateTime.now().minus(lifetime)) == true) {
            clientLock.isTimeout.set(true)
            unlock(clientLock)
        } else {
            logger.log(Level.FINEST, "Lock is not timed out!")
        }
    }

    override fun getLock(id: Any): ZookeeperClientLock? {
        return locks[id]
    }

    override fun before(id: Any): Boolean {
        return before(id, false)
    }

    protected open fun before(id: Any, completeAnswer: Boolean): Boolean {
        val time=System.currentTimeMillis()
        val lockId = getLockId(id)
        val futureToWait = CompletableFuture<Boolean>()
        if (maxLifetime.toMillis() > 0) {
            futureToWait.orTimeout(maxLifetime.toMillis(), TimeUnit.MILLISECONDS);
        }
        try {
            zooKeeper.create(lockId, byteArrayOf(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
            val lock = getOrCreateLock(id)
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
                                futureToWait.complete(before(id, true))
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
            val result = futureToWait.get()
            val tookMs = System.currentTimeMillis() - time
            if (tookMs > normalLockTime.toMillis()) {
                logger.log(Level.SEVERE, "Lock took more then ${normalLockTime.toMillis()}ms $tookMs $id")
            }
            return result
        } catch (t: ExecutionException) {
            val parent=t.cause
            if(parent is TimeoutException){
                unlock(id)
                throw SyncTimeoutException(id,maxLifetime.toMillis(),System.currentTimeMillis()-time)
            } else{
                throw parent?:t
            }
        }
    }

    override fun after(id: Any) {
        val clientLock = getLock(id)
        if (clientLock == null) {
            logger.log(Level.SEVERE, "No lock for  $id")
        } else {
            logger.log(Level.FINEST, "Lock for $id released")
            unlock(id)
            internalLock.lock()
            try {
                locks.remove(id)
            }finally {
                internalLock.unlock()
            }
        }
    }


    protected open fun getOrCreateLock(id: Any): ZookeeperClientLock {
        internalLock.lock()
        try {
            val clientLock = getLock(id)
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
     */
    protected open fun unlock(id: Any) {
        val lockId = getLockId(id)
        internalLock.lock()
        try {
            zooKeeper.delete(lockId, -1)
        } catch (t: Throwable) {
            logger.log(Level.WARNING, "Error unlocking lock ! ${t.javaClass}:${t.message}", t)
        } finally {
            internalLock.unlock()
        }
    }

    protected open fun getLockId(id: Any) = "$pathPrefix${id.longHash()}"
}