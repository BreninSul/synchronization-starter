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

package io.github.breninsul.synchronizationstarter.local

import io.github.breninsul.synchronizationstarter.SyncRunner
import io.github.breninsul.synchronizationstarter.exception.SyncTimeoutException
import io.github.breninsul.synchronizationstarter.service.local.LocalClearDecorator
import io.github.breninsul.synchronizationstarter.service.local.LocalSynchronizationService
import io.github.breninsul.synchronizationstarter.service.sync
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.LocalDateTime
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.thread

class ClearedLocalSynchronizationServiceTest {
    protected val logger = Logger.getLogger(this.javaClass.name)
    fun getClearSyncService(
        lockLifetime: Duration,
        lockTimeout: Duration,
    ): LocalClearDecorator {
        return LocalClearDecorator(lockLifetime, lockTimeout, Duration.ofMillis(10), LocalSynchronizationService(Duration.ofMillis(100)))
    }

    @Test
    fun `test clear10`() {
        for (i in 1..10) {
            `test clear`()
        }
    }

    @Test
    fun `test clear`() {
        val clearedSyncService = getClearSyncService(Duration.ofSeconds(100), Duration.ofMillis(10))
        val testSyncId = "Test"
        val time=System.currentTimeMillis()
        // Call two threads with the same task
        val exceptions= mutableListOf<SyncTimeoutException>()
        val result1= SyncRunner.runSyncTask(clearedSyncService, Duration.ofSeconds(1), exceptions)
        val result2= SyncRunner.runSyncTask(clearedSyncService, Duration.ofSeconds(1), exceptions)
        val result3= SyncRunner.runSyncTask(clearedSyncService, Duration.ofSeconds(1), exceptions)
        // wait till end
        result1.job!!.join()
        result2.job!!.join()
        result3.job!!.join()
        val took=System.currentTimeMillis()-time
        // sure that one of treads ended with exceptions
        Assertions.assertEquals(2, exceptions.size)
        Assertions.assertTrue(took>=1000,"Took ${took}<1000")
        Assertions.assertTrue(took<2000,"Took ${took}>2000")
        logger.log(Level.INFO, "Delay was ${took}")
    }

    @Test
    fun `test sync10`() {
        for (i in 1..10) {
            `test sync`()
        }
    }

    @Test
    fun `test sync`() {
        val clearedSyncService = getClearSyncService(Duration.ofSeconds(100), Duration.ofSeconds(10))
        val result1= SyncRunner.runSyncTask(clearedSyncService, Duration.ofSeconds(1), mutableListOf())
        val result2= SyncRunner.runSyncTask(clearedSyncService, Duration.ofSeconds(1), mutableListOf())
        result1.job!!.join()
        result2.job!!.join()
        // we can't be sure about threads order, sort start and end time
        val timePairs = listOf(result1.toTimePair(), result2.toTimePair()).sortedBy { it.first }
        // check that there we ordered process
        assert(timePairs[1].first > timePairs[0].second)
        val delay = Duration.between(timePairs[0].second, timePairs[1].first)
        logger.log(Level.INFO, "Delay was ${delay.toMillis()}")
    }

}
