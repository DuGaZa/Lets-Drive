package com.dugaza.letsdrive.dto.review

import com.dugaza.letsdrive.validator.CustomValidator
import java.util.UUID

class ModifyReviewRequest(
    @field:CustomValidator.NotNull(message = "reviewId는 필수 입력값입니다.")
    val reviewId: UUID,
    @field:CustomValidator.NotNull(message = "evaluationResultList는 필수 입력값입니다.")
    val evaluationResultList: List<UUID>,
    @field:CustomValidator.NotNull(message = "score는 필수 입력값입니다.")
    val score: Double,
    @field:CustomValidator.NotBlank(message = "content는 필수 입력값입니다.")
    @field:CustomValidator.Size(
        min = 1,
        max = 4000,
        message = "content는 1자 이상 4000자 이하로 작성해주세요.",
    )
    val content: String,
)
