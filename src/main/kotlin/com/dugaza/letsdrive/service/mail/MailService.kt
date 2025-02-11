package com.dugaza.letsdrive.service.mail

import com.dugaza.letsdrive.util.RedisUtil
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class MailService(
    @Value("\${spring.mail.properties.sender.email}")
    private val senderEmail: String,
    @Value("\${spring.data.redis.email-token-duration}")
    private val duration: Long,
    private val mailSender: JavaMailSender,
    private val redisUtil: RedisUtil,
) {
    fun sendMail(
        userId: String,
        nickname: String,
        toEmail: String,
    ) {
        if (redisUtil.getValue(toEmail) != null) {
            redisUtil.delete(toEmail)
        }
        val message = createMailForm(userId, nickname, toEmail)

        mailSender.send(message)
    }

    fun verifyEmail(token: String): Pair<String, String>? {
        val dataMap = redisUtil.getHashValue(token, "userId", "toEmail") ?: return null
        redisUtil.delete(token)

        return Pair(dataMap["userId"]!!, dataMap["toEmail"]!!)
    }

    private fun createMailForm(
        userId: String,
        nickname: String,
        toEmail: String,
    ): MimeMessage {
        val token = UUID.randomUUID().toString()
        val dataMap = mapOf("userId" to userId, "toEmail" to toEmail)
        redisUtil.setHashValueExpire(token, dataMap, duration, TimeUnit.MINUTES)

        val subject = "안녕하세요, $nickname 님! Let's Drive 이메일 인증을 완료해주세요."
        val htmlContent = setContext(nickname, "http://localhost:8080/api/mail/verify-email?token=$token")

        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(senderEmail, "드가자 - 드라이브가자(Let's Drive)")

        helper.setTo(toEmail)
        helper.setSubject(subject)
        helper.setText(htmlContent, true)

        return message
    }

    private fun setContext(
        nickname: String,
        verifyLink: String,
    ): String {
        val context = Context()
        val templateEngine = TemplateEngine()
        val templateResolver = ClassLoaderTemplateResolver()

        context.setVariables(mapOf("nickname" to nickname, "verifyLink" to verifyLink))
        templateResolver.prefix = "templates/"
        templateResolver.suffix = ".html"
        templateResolver.templateMode = TemplateMode.HTML
        templateResolver.isCacheable = false

        templateEngine.setTemplateResolver(templateResolver)

        return templateEngine.process("verify-email-template", context)
    }
}
