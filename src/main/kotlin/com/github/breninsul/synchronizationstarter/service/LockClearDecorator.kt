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

package com.github.breninsul.synchronizationstarter.service

import java.time.Duration
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KProperty

/**
 * This class is a decorator for ClearableSynchronisationService.
 *
 * @property lockLifetime Duration for how long lock will be alive.
 * @property clearDelay   Duration after which system will attempt to clear the lock handled by the ClearableSynchronisationService.
 * @property delegate     ClearableSynchronisationService to be decorated by adding synchronization features
 * @constructor Initializes a new LockClearDecorator instance with prescribed values.
 */
open class LockClearDecorator(
    protected val lockLifetime: Duration,
    protected val clearDelay: Duration,
    protected val delegate: ClearableSynchronisationService,
) : SynchronizationService by delegate {
    protected open val logger = Logger.getLogger(this.javaClass.name)
    protected open val batchTimer: Timer by lazy {   createTimer()}
    init {
        batchTimer
    }

    val counter = AtomicLong(1)

    /**
    * The function to initialize a timer which clears the locks after specified clearDelay
    * @return Timer which performs the cleanup operation
    */
   protected  fun createTimer(): Timer {
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
                logger.log(Level.FINEST, "Clear sync for ${delegate::class.simpleName} job №${counter.getAndIncrement()} took ${System.currentTimeMillis() - time}ms.")
            }
        }
        val timer = Timer("Clear-${delegate::class.simpleName}")
        timer.scheduleAtFixedRate(task, clearDelay.toMillis(), clearDelay.toMillis())
        return timer
    }

}
