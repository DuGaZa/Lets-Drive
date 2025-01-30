package com.dugaza.letsdrive.validator

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

class ValidationTestVo(
    @field:NotNull(message = "{FILE_001}")
    val id: UUID,
    @field:NotBlank(message = "{FILE_002}")
    val title: String,
)
