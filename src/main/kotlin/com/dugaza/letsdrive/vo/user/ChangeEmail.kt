package com.dugaza.letsdrive.vo.user

import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.validator.CustomValidator
import jakarta.validation.constraints.Email

class ChangeEmail(
    @field:Email
    @field:CustomValidator.NotBlank(ErrorCode.DEFAULT_NOT_BLANK_MESSAGE)
    val email: String,
)
