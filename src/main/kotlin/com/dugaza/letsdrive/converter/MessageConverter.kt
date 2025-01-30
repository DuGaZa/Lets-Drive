package com.dugaza.letsdrive.converter

import com.dugaza.letsdrive.exception.ErrorCode
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.stereotype.Component
import java.util.Locale

@Component
class MessageConverter(
    private val messageSource: ResourceBundleMessageSource,
) {
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
