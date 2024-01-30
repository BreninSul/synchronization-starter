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

import com.github.breninsul.synchronizationstarter.service.LocalSynchronizationService
import com.github.breninsul.synchronizationstarter.service.LockClearDecorator
import com.github.breninsul.synchronizationstarter.service.SynchronizationService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.Scheduled

@ConditionalOnClass(Scheduled::class)
@ConditionalOnProperty(prefix = "synchronisation", name = ["disabled"], matchIfMissing = true, havingValue = "false")
@AutoConfiguration
@EnableConfigurationProperties(SynchronisationProperties::class)
class SynchronisationAutoconfiguration {
    @Bean
    @ConditionalOnMissingBean(SynchronizationService::class)
    @ConditionalOnProperty(prefix = "synchronisation", name = ["mode"], matchIfMissing = true, havingValue = "LOCAL")
    fun getLocalSynchronizationService(synchronisationProperties: SynchronisationProperties): SynchronizationService {
        val local = LocalSynchronizationService()
        if (synchronisationProperties.lockTimeout.toMillis() < 1) {
            return local
        } else {
            val cleared = LockClearDecorator(synchronisationProperties.lockTimeout, synchronisationProperties.lockLifetime,synchronisationProperties.clearDelay, local)
            return cleared
        }
    }
}
