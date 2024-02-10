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

package com.github.breninsul.synchronizationstarter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Configuration properties related to Synchronisation.
 *
 * @property disabled A flag indicating whether Synchronisation is disabled.
 * @property lockTimeout The amount of time that Synchronisation locks can be held before they're timed out.
 * @property lockLifetime The lifespan of a Synchronisation lock.
 * @property clearDelay The delay between clearing of expired Synchronisation locks.
 * @property mode The mode of Synchronisation, which can be either LOCAL, POSTGRES, or ZOOKEEPER.
 * @property normalLockTime The typical duration for a Synchronisation lock.
 */
@ConfigurationProperties("synchronisation")
data class SynchronisationProperties(
    var disabled: Boolean = false,
    var lockTimeout: Duration = Duration.ofMinutes(10),
    var lockLifetime: Duration = Duration.ofMinutes(30),
    var clearDelay: Duration = Duration.ofMinutes(1),
    var mode: MODE = MODE.LOCAL,
    var normalLockTime: Duration = Duration.ofMillis(100),
) {
    /**
     * Enum representing the possible modes of operation for Synchronisation.
     */
    enum class MODE {
        LOCAL,
        POSTGRES,
        ZOOKEEPER,
    }
}
