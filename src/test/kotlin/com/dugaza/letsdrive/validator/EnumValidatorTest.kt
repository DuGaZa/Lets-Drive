package com.dugaza.letsdrive.validator

import jakarta.validation.ConstraintViolation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
class EnumValidatorTest : ValidatorTestBase() {
    @Test
    fun `should pass when status is valid and case matches`() {
        val user = TestUser(status = "ACTIVE")
        val violations: Set<ConstraintViolation<TestUser>> = validator.validate(user)
        assertEquals(0, violations.size)
    }

    @Test
    fun `should fail when status is invalid`() {
        val user = TestUser(status = "ACTIVED")
        val violations: Set<ConstraintViolation<TestUser>> = validator.validate(user)
        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("Status must be ACTIVE, INACTIVE, or PENDING", violation.message)
        assertEquals("status", violation.propertyPath.toString())
    }

    @Test
    fun `should fail when status is null and allowNull is false`() {
        val user = TestUser(status = null)
        val violations: Set<ConstraintViolation<TestUser>> = validator.validate(user)
        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("Status must be ACTIVE, INACTIVE, or PENDING", violation.message)
        assertEquals("status", violation.propertyPath.toString())
    }

    @Test
    fun `should pass when status is null and allowNull is true`() {
        val user = TestUserAllowNull(status = null)
        val violations: Set<ConstraintViolation<TestUserAllowNull>> = validator.validate(user)
        assertEquals(0, violations.size)
    }

    @Test
    fun `should pass when status is valid and case does not match but ignoreCase is true`() {
        val user = TestUserIgnoreCase(status = "active")
        val violations: Set<ConstraintViolation<TestUserIgnoreCase>> = validator.validate(user)
        assertEquals(0, violations.size)
    }

    @Test
    fun `should fail when status is invalid even if ignoreCase is true`() {
        val user = TestUserIgnoreCase(status = "pendng")
        val violations: Set<ConstraintViolation<TestUserIgnoreCase>> = validator.validate(user)
        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("Status must be ACTIVE, INACTIVE, or PENDING", violation.message)
        assertEquals("status", violation.propertyPath.toString())
    }
}
