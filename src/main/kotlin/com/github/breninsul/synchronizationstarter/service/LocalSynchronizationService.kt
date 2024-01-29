package com.github.breninsul.synchronizationsatrter.service

import com.github.breninsul.synchronizationsatrter.dto.ClientLock
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.locks.StampedLock
import java.util.logging.Level
import java.util.logging.Logger

open class LocalSynchronizationService : ClearableSynchronisationService {
    protected open val logger = Logger.getLogger(this.javaClass.name)

    protected open val locks: ConcurrentMap<Any, ClientLock> = ConcurrentHashMap()
    override fun getAllLocks(lifetime: Duration): List<Pair<Any, ClientLock>> {
        val clearBefore = LocalDateTime.now().minus(lifetime)
        return locks.filter { it.value.createdAt.isAfter(clearBefore) }.map { it.key to it.value }
    }

    override fun clear(id: Any) {
        val clientLock = locks[id]
        if (clientLock?.lock?.isWriteLocked == false) {
            locks.remove(id)
            if (clientLock?.lock?.isWriteLocked == true) {
                locks[id] = clientLock
                logger.log(Level.SEVERE, "Lock for ${id} was cleared but became locked after deletion!")
            }
        }
    }

    override fun unlockTimeOuted(id: Any, lifetime: Duration) {
        val clientLock = locks[id]
        if (clientLock?.createdAt?.isAfter(LocalDateTime.now().minus(lifetime)) == true) {
            unlock(clientLock)
        }else{
            logger.log(Level.FINEST, "Lock is not timed out!")
        }
    }

    override fun before(id: Any): Boolean {
        logger.log(Level.FINEST, "Lock for ${id} set")
        val time = System.currentTimeMillis()
        val lock = getLock(id)
        val locked: Boolean = lock.lock.isWriteLocked
        val stamp: Long = lock.lock.writeLock()
        lock.stamp = (stamp)
        val tookMs = System.currentTimeMillis() - time
        logger.log(Level.FINEST, "Lock $id is locked : $locked .Lock Took $tookMs ms")
        if (tookMs > 100) {
            logger.log(Level.SEVERE, "Lock took more then 100ms $tookMs $id")
        }
        return locked
    }


    protected open fun getLock(id: Any): ClientLock {
        val clientLock = locks[id]
        return if (clientLock == null) {
            val lock = ClientLock()
            locks[id] = lock
            lock
        } else {
            clientLock
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


    protected open fun unlock(l: ClientLock) {
        try {
            val lock: StampedLock = l.lock
            lock.unlockWrite(l.stamp!!)
        } catch (t: Throwable) {
            logger.log(Level.WARNING, "Error unlocking lock ! ${t.javaClass}:${t.message}", t)
        }
    }

}