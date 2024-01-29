package com.github.breninsul.synchronizationsatrter.service

import java.time.Duration
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level
import java.util.logging.Logger

open class LockClearDecorator(
    protected val lockLifetime: Duration,
    protected val clearDelay: Duration,
    protected val delegate: ClearableSynchronisationService,
) : SynchronizationService by delegate {
    protected open val logger = Logger.getLogger(this.javaClass.name)
    protected open val batchTimer: Timer by lazy {   createTimer()}
    val counter = AtomicLong(1)
   protected open fun createTimer(): Timer {
        val task = object : TimerTask() {
            override fun run() {
                val time = System.currentTimeMillis()
                 try {
                    val toClear=delegate.getAllLocks(lifetime = lockLifetime)
                    toClear
                        .filter { it.second.lock.isWriteLocked }
                        .forEach { l ->
                            delegate.unlockTimeOuted(l.first,lockLifetime)
                            logger.log(Level.SEVERE, "Lock has been blocked before clear :${l.first}. Took ${System.currentTimeMillis()-l.second.createdAt.toEpochSecond(ZoneOffset.of(ZoneId.systemDefault().id))}")
                        }
                    val removed = toClear.map {
                        delegate.clear(it.first)
                        it.first
                    }.joinToString(";")
                    if (removed.isNotBlank()) {
                        logger.log(Level.FINEST, "Lock for $removed removed ")
                    }
                } catch (t: Throwable) {
                    "Error clear locks ${t.javaClass}:${t.message}"
                }
                logger.log(Level.FINEST, "Clear sync for ${delegate::class.simpleName} job №${counter.getAndIncrement()} took ${System.currentTimeMillis() - time}ms."
                )
            }
        }
        val timer = Timer("Clear-${delegate::class.simpleName}")
        timer.scheduleAtFixedRate(task, clearDelay.toMillis(), clearDelay.toMillis())
        return timer
    }

}