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

package com.github.breninsul.synchronizationstarter.zookeeper

import com.github.breninsul.synchronizationstarter.exception.SyncTimeoutException
import com.github.breninsul.synchronizationstarter.service.local.LocalClearDecorator
import com.github.breninsul.synchronizationstarter.service.local.LocalSynchronizationService
import com.github.breninsul.synchronizationstarter.service.sync
import com.github.breninsul.synchronizationstarter.service.zookeeper.ZookeeperClearDecorator
import com.github.breninsul.synchronizationstarter.service.zookeeper.ZookeeperSynchronizationService
import org.apache.curator.test.TestingServer
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.thread

class ClearedZookeeperSynchronizationServiceTest {
    protected val logger = Logger.getLogger(this.javaClass.name)
    private val latch = CountDownLatch(1)

    val testingServer = TestingServer(2181, true)
    fun getZooKeeper(): ZooKeeper {
        return ZooKeeper("127.0.0.1:2181", 1000000) { event ->
            if (event.state == Watcher.Event.KeeperState.SyncConnected) {
                latch.countDown()
            }
        }
    }
    fun getSyncService(lifetime:Duration): ZookeeperSynchronizationService {
        return ZookeeperSynchronizationService(getZooKeeper(), lifetime)
    }
    fun getClearSyncService(
        lockLifetime: Duration,
        lockTimeout: Duration,
    ): ZookeeperClearDecorator {
        return ZookeeperClearDecorator(lockLifetime, lockTimeout, Duration.ofMillis(10), getSyncService(lockTimeout))
    }

    @Test
    fun `test clear10`() {
        for (i in 1..10) {
            `test clear`()
        }
    }

    @Test
    fun `test clear`() {
        val clearedSyncService = getClearSyncService(Duration.ofSeconds(100), Duration.ofMillis(100))
        val testSyncId = "Test"
        val time=System.currentTimeMillis()
        // Call two threads with the same task
        val exceptions= mutableListOf<SyncTimeoutException>()
        val jobThread =
            thread(start = true) {
                try {
                    clearedSyncService.sync(testSyncId) {
                        Thread.sleep(Duration.ofSeconds(1))
                    }
                }catch (t:SyncTimeoutException){
                    exceptions.add(t)
                }
            }
        val jobThread2 =
            thread(start = true) {
                try {
                    clearedSyncService.sync(testSyncId) {
                        Thread.sleep(Duration.ofSeconds(1))
                    }
                }catch (t:SyncTimeoutException){
                    exceptions.add(t)
                }
            }
        val jobThread3 =
            thread(start = true) {
                try {
                    clearedSyncService.sync(testSyncId) {
                        Thread.sleep(Duration.ofSeconds(1))
                    }
                }catch (t:SyncTimeoutException){
                    exceptions.add(t)
                }
            }
        // wait till end
        jobThread.join()
        jobThread2.join()
        jobThread3.join()
        val took=System.currentTimeMillis()-time
        // sure that one of treads ended with exceptions
        if (exceptions.size!=2){
            logger.log(Level.SEVERE,"")
        }
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
