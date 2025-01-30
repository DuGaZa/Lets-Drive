package com.dugaza.letsdrive.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource

@Configuration
class MessageSourceConfig {
    @Bean
    fun messageSource(): ResourceBundleMessageSource {
        val messageSource = ResourceBundleMessageSource()
        messageSource.setBasename("messages/messages")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }
}
