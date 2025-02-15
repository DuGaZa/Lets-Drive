package com.dugaza.letsdrive.dto.review

import java.util.UUID

class DeleteReviewRequest(
//    @field:CustomValidator.NotBlank(message = "reviewId는 필수 입력값입니다.")
    val reviewId: UUID,
//    @field:CustomValidator.NotBlank(message = "userId는 필수 입력값입니다.")
    val userId: UUID,
)
