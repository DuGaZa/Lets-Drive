package com.dugaza.letsdrive.dto.review

import jakarta.validation.constraints.NotBlank
import java.util.*

class DeleteReviewRequest(
    @field:NotBlank(message = "reviewId는 필수 입력값입니다.")
    val reviewId: UUID,
    @field:NotBlank(message = "userId는 필수 입력값입니다.")
    val userId: UUID,
)