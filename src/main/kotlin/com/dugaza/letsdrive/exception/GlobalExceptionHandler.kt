package com.dugaza.letsdrive.exception

import com.dugaza.letsdrive.converter.MessageConverter
import com.dugaza.letsdrive.dto.ErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(
    private val messageConverter: MessageConverter,
) {
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode
        val code = errorCode.code
        val body =
            ErrorResponse(
                code = code,
                message = messageConverter.getMessage(code),
            )
        return ResponseEntity.status(errorCode.status).body(body)
    }
}
