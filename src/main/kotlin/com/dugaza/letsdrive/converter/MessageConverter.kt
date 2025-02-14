package com.dugaza.letsdrive.converter

import com.dugaza.letsdrive.exception.ErrorCode
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import java.util.Locale

class MessageConverter {
    private val messageSource: ResourceBundleMessageSource =
        ResourceBundleMessageSource().apply {
            setBasename("messages/messages")
            setDefaultEncoding("UTF-8")
        }

    private val locale: Locale
        get() = LocaleContextHolder.getLocale()

    fun getMessage(
        code: String,
        vararg args: Any?,
    ): String {
        return try {
            messageSource.getMessage(code, args, locale)
        } catch (e: NoSuchMessageException) {
            messageSource.getMessage(ErrorCode.INVALID_ERROR_CODE.code, args, locale)
        }
    }
}
