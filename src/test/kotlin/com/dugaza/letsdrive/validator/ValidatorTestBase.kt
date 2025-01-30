package com.dugaza.letsdrive.validator

import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

abstract class ValidatorTestBase {
    companion object {
        lateinit var validatorFactory: ValidatorFactory
        lateinit var validator: Validator

        @BeforeAll
        @JvmStatic
        fun setUpValidator() {
            validatorFactory = Validation.buildDefaultValidatorFactory()
            validator = validatorFactory.validator
        }

        @AfterAll
        @JvmStatic
        fun close() {
            validatorFactory.close()
        }
    }
}
