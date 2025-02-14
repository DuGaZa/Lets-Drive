package com.dugaza.letsdrive.validator

import com.dugaza.letsdrive.exception.ErrorCode
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

class CustomValidator {
    @Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Constraint(validatedBy = [CustomValidatorEmbodiment.NotNullValidator::class])
    annotation class NotNull(
        val errorCode: ErrorCode = ErrorCode.DEFAULT_NOT_NULL_MESSAGE,
        val message: String = "",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = [],
    )

    @Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Constraint(validatedBy = [CustomValidatorEmbodiment.NotBlankValidator::class])
    annotation class NotBlank(
        val errorCode: ErrorCode = ErrorCode.DEFAULT_NOT_BLANK_MESSAGE,
        val message: String = "",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = [],
    )

    @Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Constraint(validatedBy = [CustomValidatorEmbodiment.SizeValidator::class])
    annotation class Size(
        val min: Int = 0,
        val max: Int = Integer.MAX_VALUE,
        val errorCode: ErrorCode = ErrorCode.DEFAULT_SIZE_MESSAGE,
        val message: String = "",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = [],
    )

    @Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Constraint(validatedBy = [CustomValidatorEmbodiment.MinValidator::class])
    annotation class Min(
        val value: Long = 0,
        val errorCode: ErrorCode = ErrorCode.DEFAULT_MIN_MESSAGE,
        val message: String = "",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = [],
    )

    @Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Constraint(validatedBy = [CustomValidatorEmbodiment.MaxValidator::class])
    annotation class Max(
        val value: Long = 0,
        val errorCode: ErrorCode = ErrorCode.DEFAULT_MAX_MESSAGE,
        val message: String = "",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = [],
    )

    @Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    @Constraint(validatedBy = [CustomValidatorEmbodiment.RangeValidator::class])
    annotation class Range(
        val from: Long = 0,
        val to: Long = 0,
        val errorCode: ErrorCode = ErrorCode.DEFAULT_RANGE_MESSAGE,
        val message: String = "",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = [],
    )
}
