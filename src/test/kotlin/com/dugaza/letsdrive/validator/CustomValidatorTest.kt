package com.dugaza.letsdrive.validator

import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import jakarta.validation.ConstraintViolation
import jakarta.validation.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CustomValidatorTest : ValidatorTestBase() {
    @Test
    fun `Custom NotNull Success`() {
        val vo = ValidationTestVo(Any(), "this is a test", "abc", 10, 255, 10)
        val violations: Set<ConstraintViolation<ValidationTestVo>> = validator.validate(vo)
        assertEquals(0, violations.size)
    }

    @Test
    fun `Custom NotNull Fail`() {
        val vo = ValidationTestVo(null, "this is a test", "abc", 10, 255, 10)
        val ex = assertThrows<ValidationException> { validator.validate(vo) }
        val t = ex.cause
        assertTrue(t is BusinessException)
        assertEquals("필수 파라미터가 없습니다.", ex.cause!!.message)
        assertEquals(ErrorCode.DEFAULT_NOT_NULL_MESSAGE, t.errorCode)
    }

    @Test
    fun `Custom NotBlank Success`() {
        val vo = ValidationTestVo(Any(), "this is a test", "abc", 10, 255, 10)
        val violations: Set<ConstraintViolation<ValidationTestVo>> = validator.validate(vo)
        assertEquals(0, violations.size)
    }

    @Test
    fun `Custom NotBlank Fail`() {
        val vo = ValidationTestVo(Any(), "", "abc", 10, 255, 10)
        val ex = assertThrows<ValidationException> { validator.validate(vo) }
        val t = ex.cause
        assertTrue(t is BusinessException)
        assertEquals("필수 파라미터가 없거나 비어있습니다.", ex.cause!!.message)
        assertEquals(ErrorCode.DEFAULT_NOT_BLANK_MESSAGE, t.errorCode)
    }

    @Test
    fun `Custom Size Success`() {
        val vo = ValidationTestVo(Any(), "this is a test", "abc", 10, 255, 10)
        val violations: Set<ConstraintViolation<ValidationTestVo>> = validator.validate(vo)
        assertEquals(0, violations.size)
    }

    @Test
    fun `Custom Size Fail`() {
        val vo = ValidationTestVo(Any(), "this is a test", "abcdefghijklmnop", 10, 255, 10)
        val ex = assertThrows<ValidationException> { validator.validate(vo) }
        val t = ex.cause
        assertTrue(t is BusinessException)
        assertEquals("size 파라미터의 길이는 1 ~ 10 사이로 지정 가능합니다.", ex.cause!!.message)
        assertEquals(ErrorCode.DEFAULT_SIZE_MESSAGE, t.errorCode)
    }

    @Test
    fun `Custom Min Success`() {
        val vo = ValidationTestVo(Any(), "this is a test", "abc", 10, 255, 10)
        val violations: Set<ConstraintViolation<ValidationTestVo>> = validator.validate(vo)
        assertEquals(0, violations.size)
    }

    @Test
    fun `Custom Min Fail`() {
        val vo = ValidationTestVo(Any(), "this is a test", "abc", 9, 255, 10)
        val ex = assertThrows<ValidationException> { validator.validate(vo) }
        val t = ex.cause
        assertTrue(t is BusinessException)
        assertEquals("min 파라미터의 값은 최소 10 이상 지정 가능합니다.", ex.cause!!.message)
        assertEquals(ErrorCode.DEFAULT_MIN_MESSAGE, t.errorCode)
    }

    @Test
    fun `Custom Max Success`() {
        val vo = ValidationTestVo(Any(), "this is a test", "abc", 10, 255, 10)
        val violations: Set<ConstraintViolation<ValidationTestVo>> = validator.validate(vo)
        assertEquals(0, violations.size)
    }

    @Test
    fun `Custom Max Fail`() {
        val vo = ValidationTestVo(Any(), "this is a test", "abc", 10, 2555, 10)
        val ex = assertThrows<ValidationException> { validator.validate(vo) }
        val t = ex.cause
        assertTrue(t is BusinessException)
        assertEquals("max 파라미터의 값은 최대 255 까지 지정 가능합니다.", ex.cause!!.message)
        assertEquals(ErrorCode.DEFAULT_MAX_MESSAGE, t.errorCode)
    }

    @Test
    fun `Custom Range Success`() {
        val vo = ValidationTestVo(Any(), "this is a test", "abc", 10, 255, 10)
        val violations: Set<ConstraintViolation<ValidationTestVo>> = validator.validate(vo)
        assertEquals(0, violations.size)
    }

    @Test
    fun `Custom Range Fail`() {
        val vo = ValidationTestVo(Any(), "this is a test", "abc", 10, 255, 15)
        val ex = assertThrows<ValidationException> { validator.validate(vo) }
        val t = ex.cause
        assertTrue(t is BusinessException)
        assertEquals("range 파라미터의 범위는 1 ~ 10 사이로 지정 가능합니다.", ex.cause!!.message)
        assertEquals(ErrorCode.DEFAULT_RANGE_MESSAGE, t.errorCode)
    }
}
