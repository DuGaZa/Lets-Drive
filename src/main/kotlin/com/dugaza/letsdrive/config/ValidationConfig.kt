package com.dugaza.letsdrive.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

@Configuration
class ValidationConfig(
    private val messageSource: ResourceBundleMessageSource,
) {
    @Bean
    fun getValidator(): LocalValidatorFactoryBean {
        val factoryBean = LocalValidatorFactoryBean()
        factoryBean.setValidationMessageSource(messageSource)
        return factoryBean
    }
}
