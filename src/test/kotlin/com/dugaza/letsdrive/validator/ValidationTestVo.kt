package com.dugaza.letsdrive.validator

import com.dugaza.letsdrive.exception.ErrorCode

class ValidationTestVo(
    @field:CustomValidator.NotNull(ErrorCode.DEFAULT_NOT_NULL_MESSAGE)
    private val notNull: String?,
    @field:CustomValidator.NotBlank(ErrorCode.DEFAULT_NOT_BLANK_MESSAGE)
    private val notBlank: String,
    @field:CustomValidator.Size(1, 10, ErrorCode.DEFAULT_SIZE_MESSAGE)
    private val size: String,
    @field:CustomValidator.Min(10, ErrorCode.DEFAULT_MIN_MESSAGE)
    private val min: Int,
    @field:CustomValidator.Max(255, ErrorCode.DEFAULT_MAX_MESSAGE)
    private val max: Int,
    @field:CustomValidator.Range(1, 10, ErrorCode.DEFAULT_RANGE_MESSAGE)
    private val range: Int,
)
