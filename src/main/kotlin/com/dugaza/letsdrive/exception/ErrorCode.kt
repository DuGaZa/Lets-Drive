package com.dugaza.letsdrive.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
) {
    // File Error
    FILE_SIZE_TOO_LARGE(HttpStatus.BAD_REQUEST, "FILE_001"),
    INVALID_EXTENSION(HttpStatus.BAD_REQUEST, "FILE_002"),
    IMAGE_THUMBNAIL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_003"),
    FILE_COMPRESSION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_004"),
    FILE_DECOMPRESSION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_005"),
    NOT_FOUND_FILE_DETAIL(HttpStatus.NOT_FOUND, "FILE_006"),
    INVALID_IMAGE_DATA(HttpStatus.BAD_REQUEST, "FILE_007"),
    NOT_FOUND_FILE_MASTER(HttpStatus.NOT_FOUND, "FILE_008"),

    // User Error
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001"),

    // Course Error
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_001"),

    // Evaluation Error
    EVALUATION_NOT_FOUND(HttpStatus.NOT_FOUND, "EVALUATION_001"),
    EVALUATION_ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "EVALUATION_002"),
    EVALUATION_TYPE_CONFLICT(HttpStatus.CONFLICT, "EVALUATION_003"),
    EVALUATION_QUESTION_CONFLICT(HttpStatus.CONFLICT, "EVALUATION_004"),
    EVALUATION_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "EVALUATION_005"),
    EVALUATION_ANSWER_CONFLICT(HttpStatus.CONFLICT, "EVALUATION_006"),
    EVALUATION_RESULT_ANSWER_CONFLICT(HttpStatus.CONFLICT, "EVALUATION_007"),
    INVALID_EVALUATION_ANSWER(HttpStatus.BAD_REQUEST, "EVALUATION_008"),
    EVALUATION_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "EVALUATION_009"),

    // Review Error
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_001"),
    REVIEW_SCORE_INVALID(HttpStatus.BAD_REQUEST, "REVIEW_002"),

    // System Error
    INVALID_ERROR_CODE(HttpStatus.BAD_REQUEST, "SYSTEM_001"),

    // Validation
    DEFAULT_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALID_001"),
    DEFAULT_NOT_NULL_MESSAGE(HttpStatus.BAD_REQUEST, "VALID_002"),
    DEFAULT_NOT_BLANK_MESSAGE(HttpStatus.BAD_REQUEST, "VALID_003"),
    DEFAULT_SIZE_MESSAGE(HttpStatus.BAD_REQUEST, "VALID_004"),
    DEFAULT_MIN_MESSAGE(HttpStatus.BAD_REQUEST, "VALID_005"),
    DEFAULT_MAX_MESSAGE(HttpStatus.BAD_REQUEST, "VALID_006"),
    DEFAULT_RANGE_MESSAGE(HttpStatus.BAD_REQUEST, "VALID_007"),

    // For test
    FOO(HttpStatus.INTERNAL_SERVER_ERROR, "FOO_001"),
}
