package com.dugaza.letsdrive.dto.review

import com.dugaza.letsdrive.validator.CustomValidator
import java.util.UUID

class DeleteReviewRequest(
    @field:CustomValidator.NotNull(message = "reviewId는 필수 입력값입니다.")
    val reviewId: UUID,
    @field:CustomValidator.NotNull(message = "userId는 필수 입력값입니다.")
    val userId: UUID,
)
