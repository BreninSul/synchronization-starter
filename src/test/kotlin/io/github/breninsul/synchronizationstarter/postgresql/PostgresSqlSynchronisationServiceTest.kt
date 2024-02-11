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

package io.github.breninsul.synchronizationstarter.postgresql

import io.github.breninsul.synchronizationstarter.service.db.PostgresSQLSynchronisationService
import io.github.breninsul.synchronizationstarter.service.sync
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.PostgreSQLContainer
import java.time.Duration
import java.time.LocalDateTime
import java.util.logging.Level
import java.util.logging.Logger
import javax.sql.DataSource
import kotlin.concurrent.thread

@ExtendWith(SpringExtension::class)
@Import(DataSourceAutoConfiguration::class)
class PostgresSqlSynchronisationServiceTest {
    protected val logger = Logger.getLogger(this.javaClass.name)

    @Autowired
    lateinit var dataSource: DataSource

    fun getSyncService(): PostgresSQLSynchronisationService  {
        return PostgresSQLSynchronisationService(dataSource, Duration.ofMillis(100))
    }

    @Test
    fun `test sync the same service`() {
        var startedTime: LocalDateTime? = null
        var endedTime: LocalDateTime? = null
        val testSyncId = "Test"
        val syncService = getSyncService()
        // Call two threads with the same task
        val jobThread =
            thread(start = true) {
                syncService.sync(testSyncId) {
                    startedTime = LocalDateTime.now()
                    Thread.sleep(Duration.ofSeconds(1))
                    endedTime = LocalDateTime.now()
                }
            }
        var startedTime2: LocalDateTime? = null
        var endedTime2: LocalDateTime? = null
        val jobThread2 =
            thread(start = true) {
                syncService.sync(testSyncId) {
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
    @Test
    fun `test sync diff services`() {
        var startedTime: LocalDateTime? = null
        var endedTime: LocalDateTime? = null
        val testSyncId = "Test"
        val syncService = getSyncService()
        val syncService2 = getSyncService()
        // Call two threads with the same task
        val jobThread =
            thread(start = true) {
                syncService.sync(testSyncId) {
                    startedTime = LocalDateTime.now()
                    Thread.sleep(Duration.ofSeconds(1))
                    endedTime = LocalDateTime.now()
                }
            }
        var startedTime2: LocalDateTime? = null
        var endedTime2: LocalDateTime? = null
        val jobThread2 =
            thread(start = true) {
                syncService2.sync(testSyncId) {
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
    companion object {
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16.1-alpine3.19")

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            postgres.start()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            postgres.stop()
        }
    }
}
