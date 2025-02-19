package com.dugaza.letsdrive.validator

import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.logger
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl
import kotlin.properties.Delegates

class CustomValidatorEmbodiment {
    companion object {
        fun getAnnotatedFieldName(p1: ConstraintValidatorContext): String {
            val fieldName =
                if (p1 is ConstraintValidatorContextImpl) {
                    p1.constraintViolationCreationContexts[0].path.leafNode.name.toString()
                } else {
                    ""
                }
            println("FieldName: $fieldName")
            return fieldName
        }
    }

    class NotNullValidator : ConstraintValidator<CustomValidator.NotNull, Any> {
        private val log = logger()

        private lateinit var errorCode: ErrorCode

        override fun initialize(constraintAnnotation: CustomValidator.NotNull?) {
            this.errorCode = constraintAnnotation!!.errorCode
            log.debug("# CustomNotBlankValidator initialized, errorCode: {}", errorCode)
        }

        override fun isValid(
            p0: Any?,
            p1: ConstraintValidatorContext?,
        ): Boolean {
            log.debug("Not null validation, p0: {}", p0)
            return (p0 ?: throw BusinessException(this.errorCode)).let { true }
        }
    }

    class NotBlankValidator : ConstraintValidator<CustomValidator.NotBlank, String> {
        private val log = logger()

        private lateinit var errorCode: ErrorCode

        override fun initialize(constraintAnnotation: CustomValidator.NotBlank?) {
            this.errorCode = constraintAnnotation!!.errorCode
            log.debug("# CustomNotBlankValidator initialized, errorCode: {}", errorCode)
        }

        override fun isValid(
            p0: String?,
            p1: ConstraintValidatorContext?,
        ): Boolean {
            log.debug("Not blank validation, p0: {}", p0)
            return (!p0.isNullOrBlank() || throw BusinessException(this.errorCode)).let { true }
        }
    }

    class SizeValidator : ConstraintValidator<CustomValidator.Size, String> {
        private val log = logger()

        private lateinit var errorCode: ErrorCode
        private var min: Int by Delegates.notNull()
        private var max: Int by Delegates.notNull()

        override fun initialize(constraintAnnotation: CustomValidator.Size?) {
            this.errorCode = constraintAnnotation!!.errorCode
            this.min = constraintAnnotation.min
            this.max = constraintAnnotation.max
            log.debug("# CustomSizeValidator initialized, errorCode: {}, min: {}, max: {}", errorCode, min, max)
        }

        override fun isValid(
            p0: String?,
            p1: ConstraintValidatorContext,
        ): Boolean {
            log.debug("Size validation, p0: {}, min: {}, max: {}", p0, min, max)
            p0.takeIf { it != null && it.length in min..max }
                ?: throw BusinessException(errorCode, getAnnotatedFieldName(p1), min, max)
            return true
        }
    }

    class MinValidator : ConstraintValidator<CustomValidator.Min, Number> {
        private val log = logger()

        private lateinit var errorCode: ErrorCode
        private var value: Long by Delegates.notNull()

        override fun initialize(constraintAnnotation: CustomValidator.Min?) {
            this.errorCode = constraintAnnotation!!.errorCode
            this.value = constraintAnnotation.value
            log.debug("# CustomMinValidator initialized, errorCode: {}, value: {}", errorCode, value)
        }

        override fun isValid(
            p0: Number?,
            p1: ConstraintValidatorContext,
        ): Boolean {
            log.debug("Min validation, p0: {}, value: {}", p0, value)
            if (p0 == null) throw BusinessException(errorCode, getAnnotatedFieldName(p1), value)
            p0.toDouble().takeIf { it >= value }
                ?: throw BusinessException(errorCode, getAnnotatedFieldName(p1), value)
            return true
        }
    }

    class MaxValidator : ConstraintValidator<CustomValidator.Max, Number> {
        private val log = logger()

        private lateinit var errorCode: ErrorCode
        private var value: Long by Delegates.notNull()

        override fun initialize(constraintAnnotation: CustomValidator.Max?) {
            this.errorCode = constraintAnnotation!!.errorCode
            this.value = constraintAnnotation.value
            log.debug("# CustomMaxValidator initialized, errorCode: {}, value: {}", errorCode, value)
        }

        override fun isValid(
            p0: Number?,
            p1: ConstraintValidatorContext,
        ): Boolean {
            log.debug("Max validation, p0: {}, value: {}", p0, value)
            if (p0 == null) throw BusinessException(errorCode, getAnnotatedFieldName(p1), value)
            p0.toDouble().takeIf { it <= value }
                ?: throw BusinessException(errorCode, getAnnotatedFieldName(p1), value)
            return true
        }
    }

    class RangeValidator : ConstraintValidator<CustomValidator.Range, Number> {
        private val log = logger()

        private lateinit var errorCode: ErrorCode
        private var from: Long by Delegates.notNull()
        private var to: Long by Delegates.notNull()

        override fun initialize(constraintAnnotation: CustomValidator.Range?) {
            this.errorCode = constraintAnnotation!!.errorCode
            this.from = constraintAnnotation.from
            this.to = constraintAnnotation.to
            log.debug("# CustomRangeValidator initialized, errorCode: {}, from: {}, to: {}", errorCode, from, to)
        }

        override fun isValid(
            p0: Number?,
            p1: ConstraintValidatorContext,
        ): Boolean {
            log.debug("Range validation, p0: {}, from: {}, to: {}", p0, from, to)
            if (p0 == null) throw BusinessException(errorCode, getAnnotatedFieldName(p1), from, to)
            p0.toDouble().takeIf { it in from.toDouble()..to.toDouble() }
                ?: throw BusinessException(errorCode, getAnnotatedFieldName(p1), from, to)
            return true
        }
    }
}
