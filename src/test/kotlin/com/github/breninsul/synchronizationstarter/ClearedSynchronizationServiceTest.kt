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

package com.github.breninsul.synchronizationstarter

import com.github.breninsul.synchronizationstarter.service.LocalSynchronizationService
import com.github.breninsul.synchronizationstarter.service.LockClearDecorator
import com.github.breninsul.synchronizationstarter.service.sync
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.thread

class ClearedSynchronizationServiceTest {
    protected val syncService = LocalSynchronizationService()
    protected val logger = Logger.getLogger(this.javaClass.name)

    @Test
    fun `test clear`() {
        val clearedSyncService = LockClearDecorator(Duration.ofMillis(100), Duration.ofMillis(10),Duration.ofMillis(10), syncService)
        var startedTime: LocalDateTime? = null
        var endedTime: LocalDateTime? = null
        val testSyncId = "Test"
        // Call two threads with the same task
        val jobThread =
            thread(start = true) {
                clearedSyncService.sync(testSyncId) {
                    startedTime = LocalDateTime.now()
                    Thread.sleep(Duration.ofSeconds(1))
                    endedTime = LocalDateTime.now()
                }
            }
        var startedTime2: LocalDateTime? = null
        var endedTime2: LocalDateTime? = null
        val jobThread2 =
            thread(start = true) {
                clearedSyncService.sync(testSyncId) {
                    startedTime2 = LocalDateTime.now()
                    Thread.sleep(Duration.ofSeconds(1))
                    endedTime2 = LocalDateTime.now()
                }
            }
        // wait till end
        jobThread.join()
        jobThread2.join()
        // we can't be sure about threads order, sort start and end time
        val timePairs = listOf(startedTime!! to endedTime!!, startedTime2!! to endedTime2!!).sortedBy { it.first }

        val delay = Duration.between(timePairs[0].first, timePairs[1].first)
        assert(delay < Duration.ofSeconds(1))
        logger.log(Level.INFO, "Delay was ${delay.toMillis()}")
    }

    @Test
    fun `test sync`() {
        val clearedSyncService = LockClearDecorator(Duration.ofSeconds(100), Duration.ofMillis(10),Duration.ofMillis(10), syncService)
        var startedTime: LocalDateTime? = null
        var endedTime: LocalDateTime? = null
        val testSyncId = "Test"
        // Call two threads with the same task
        val jobThread =
            thread(start = true) {
                clearedSyncService.sync(testSyncId) {
                    startedTime = LocalDateTime.now()
                    Thread.sleep(Duration.ofSeconds(1))
                    endedTime = LocalDateTime.now()
                }
            }
        var startedTime2: LocalDateTime? = null
        var endedTime2: LocalDateTime? = null
        val jobThread2 =
            thread(start = true) {
                clearedSyncService.sync(testSyncId) {
                    startedTime2 = LocalDateTime.now()
                    Thread.sleep(Duration.ofSeconds(1))
                    endedTime2 = LocalDateTime.now()
                }
            }
        // wait till end
        jobThread.join()
        jobThread2.join()
        // we can't be sure about threads order, sort start and end time
        val timePairs = listOf(startedTime!! to endedTime!!, startedTime2!! to endedTime2!!).sortedBy { it.first }
        // check that there we ordered process
        assert(timePairs[1].first > timePairs[0].second)
        val delay = Duration.between(timePairs[0].second, timePairs[1].first)
        logger.log(Level.INFO, "Delay was ${delay.toMillis()}")
    }
}
