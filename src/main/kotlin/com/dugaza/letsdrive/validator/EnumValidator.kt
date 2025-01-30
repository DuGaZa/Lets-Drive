package com.dugaza.letsdrive.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class EnumValidator : ConstraintValidator<ValidEnum, String> {
    private lateinit var enumValues: Array<out Enum<*>>
    private var allowNull = false
    private var ignoreCase = false

    override fun initialize(constraintAnnotation: ValidEnum) {
        enumValues = constraintAnnotation.enumClass.java.enumConstants
        allowNull = constraintAnnotation.allowNull
        ignoreCase = constraintAnnotation.ignoreCase
    }

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value == null) {
            return allowNull
        }
        return enumValues.any { it.name.equals(value, ignoreCase = true) }
    }
}
