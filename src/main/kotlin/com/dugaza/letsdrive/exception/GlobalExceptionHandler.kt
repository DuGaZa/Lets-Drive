package com.dugaza.letsdrive.exception

import com.dugaza.letsdrive.dto.ErrorResponse
import com.dugaza.letsdrive.logger
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = logger()

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode
        val code = errorCode.code
        val body =
            ErrorResponse(
                code = code,
                message = e.message!!,
            )
        log.debug("Business exception occurred, error code: {}", errorCode)
        return ResponseEntity.status(errorCode.status).body(body)
    }

    /**
     * MethodArgumentNotValidException: Query Parameter 를 받을때 발생.
     * - Not-null object 에 Null 을 바인딩 할때 생기는 예외.
     *
     * MismatchedInputException: RequestBody 를 받을때 발생.
     * - JSON data binding(jackson)시 Not-null object 에 Null 을 바인딩 할때 생기는 예외.
     * - 상위에 있는 HttpMessageNotReadableException 대신 실제 발생한 MismatchedInputException 을 사용함.
     *
     * <code>@NotNull</code> 어노테이션 보다 일찍 예외가 발생하여 따로 핸들링 해야한다.
     */
    @ExceptionHandler(exception = [MismatchedInputException::class, MethodArgumentNotValidException::class])
    fun handleMissingKotlinParameterException(e: Throwable): ResponseEntity<ErrorResponse> {
        // FIXME: 추후 message converter 에 파라미터를 박을수 있게 한 후 원몬 메시지를 보내면 어떨까 싶다.
        return handleBusinessException(BusinessException(ErrorCode.DEFAULT_VALIDATION_FAILED))
    }
}
