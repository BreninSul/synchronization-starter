

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
@ConditionalOnProperty(prefix = "synchronisation",name = ["disabled"], matchIfMissing = true, havingValue = "false")
@AutoConfiguration
@EnableConfigurationProperties(SynchronisationProperties::class)

class SynchronisationAutoconfiuration {
    @Bean
    @ConditionalOnMissingBean(SynchronizationService::class)
    @ConditionalOnProperty(prefix = "synchronisation",name = ["mode"], matchIfMissing = true, havingValue = "LOCAL")
    fun getLocalSynchronizationService(synchronisationProperties: SynchronisationProperties): SynchronizationService {
        val local=LocalSynchronizationService()
        if (synchronisationProperties.lockTimeout.toMillis()<1){
            return local
        } else{
            val cleared=LockClearDecorator(synchronisationProperties.lockTimeout,synchronisationProperties.clearDelay,local)
            return cleared
        }
    }

}