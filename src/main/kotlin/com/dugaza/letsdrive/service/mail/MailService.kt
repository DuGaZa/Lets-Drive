package com.dugaza.letsdrive.service.mail

import com.dugaza.letsdrive.util.RedisUtil
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
    private val templateEngine: TemplateEngine =
        TemplateEngine().apply {
            val templateResolver =
                ClassLoaderTemplateResolver().apply {
                    prefix = "templates/"
                    suffix = ".html"
                    templateMode = TemplateMode.HTML
                    isCacheable = false
                }
            setTemplateResolver(templateResolver)
        }

    fun sendMail(mailContent: MailContent) {
        val context = Context().apply { setVariables(mailContent.contextVariables) }
        val htmlContent = templateEngine.process(mailContent.templateName, context)

        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(senderEmail, "드가자 - 드라이브가자(Let's Drive)")
        helper.setTo(mailContent.toEmail)
        helper.setSubject(mailContent.subject)
        helper.setText(htmlContent, true)

        mailSender.send(message)
    }

    fun sendVerificationMail(
        userId: UUID,
        nickname: String,
        toEmail: String,
    ) {
        redisUtil.getValue(toEmail)?.let { redisUtil.delete(it) }

        val token = UUID.randomUUID().toString()
        val dataMap = mapOf("userId" to userId.toString(), "toEmail" to toEmail)
        redisUtil.setHashValueExpire(token, dataMap, duration, TimeUnit.MINUTES)

        val subject = "안녕하세요, $nickname 님! Let's Drive 이메일 인증을 완료해주세요."
        val verifyLink = "http://localhost:8080/api/mail/verify-email?token=$token"

        val mailContent =
            MailContent(
                toEmail = toEmail,
                subject = subject,
                templateName = "verify-email-template",
                contextVariables = mapOf("nickname" to nickname, "verifyLink" to verifyLink),
            )

        sendMail(mailContent)
    }

    fun verifyMail(token: String): Pair<String, String>? {
        val dataMap = redisUtil.getHashValue(token, "userId", "toEmail") ?: return null
        redisUtil.delete(token)

        return Pair(dataMap["userId"]!!, dataMap["toEmail"]!!)
    }
}

data class MailContent(
    val toEmail: String,
    val subject: String,
    val templateName: String,
    val contextVariables: Map<String, Any>,
)
