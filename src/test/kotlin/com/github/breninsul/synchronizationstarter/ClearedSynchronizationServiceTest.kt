package com.github.breninsul.synchronizationstarter

import com.github.breninsul.synchronizationstarter.service.LocalSynchronizationService
import com.github.breninsul.synchronizationstarter.service.sync
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.thread

class LocalSynchronizationServiceTest {
    protected val syncService = LocalSynchronizationService()
    protected val logger = Logger.getLogger(this.javaClass.name)

    @Test
    fun `test sync`() {
        var startedTime: LocalDateTime? = null
        var endedTime: LocalDateTime? = null
        val testSyncId = "Test"
        //Call two threads with the same task
        val jobThread = thread(start = true) {
            syncService.sync(testSyncId) {
                startedTime = LocalDateTime.now()
                Thread.sleep(Duration.ofSeconds(1))
                endedTime = LocalDateTime.now()
            }
        }
        var startedTime2: LocalDateTime? = null
        var endedTime2: LocalDateTime? = null
        val jobThread2 = thread(start = true) {
            syncService.sync(testSyncId) {
                startedTime2 = LocalDateTime.now()
                Thread.sleep(Duration.ofSeconds(1))
                endedTime2 = LocalDateTime.now()
            }
        }
        //wait till end
        jobThread.join()
        jobThread2.join()
        //we can't be sure about threads order, sort start and end time
        val timePairs = listOf(startedTime!! to endedTime!!, startedTime2!! to endedTime2!!).sortedBy { it.first }
        //check that there we ordered process
        assert(timePairs[1].first > timePairs[0].second)
        val delay = Duration.between(timePairs[0].second, timePairs[1].first)
        logger.log(Level.INFO,"Delay was ${delay.toMillis()}")
    }
}