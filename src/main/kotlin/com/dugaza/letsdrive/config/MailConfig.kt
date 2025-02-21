package com.dugaza.letsdrive.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
class MailConfig(
    @Value("\${spring.mail.host}")
    private val host: String,
    @Value("\${spring.mail.port}")
    private val port: Int,
    @Value("\${spring.mail.username}")
    private val username: String,
    @Value("\${spring.mail.password}")
    private val password: String,
    @Value("\${spring.mail.properties.mail.smtp.auth}")
    private val auth: Boolean,
    @Value("\${spring.mail.properties.mail.smtp.starttls.enable}")
    private val starttlsEnable: Boolean,
    @Value("\${spring.mail.properties.mail.smtp.starttls.required}")
    private val starttlsRequired: Boolean,
    @Value("\${spring.mail.properties.mail.smtp.connectiontimeout}")
    private val connectionTimeout: Int,
    @Value("\${spring.mail.properties.mail.smtp.timeout}")
    private val timeout: Int,
    @Value("\${spring.mail.properties.mail.smtp.writetimeout}")
    private val writeTimeout: Int,
) {
    @Bean
    fun mailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = host
        mailSender.port = port
        mailSender.username = username
        mailSender.password = password
        mailSender.javaMailProperties.apply {
            put("mail.smtp.auth", auth)
            put("mail.smtp.starttls.enable", starttlsEnable)
            put("mail.smtp.starttls.required", starttlsRequired)
            put("mail.smtp.connectiontimeout", connectionTimeout)
            put("mail.smtp.timeout", timeout)
            put("mail.smtp.writetimeout", writeTimeout)
        }
        return mailSender
    }
}
