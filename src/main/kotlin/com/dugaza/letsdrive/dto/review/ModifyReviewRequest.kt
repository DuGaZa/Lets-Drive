package com.dugaza.letsdrive.dto.review

import com.dugaza.letsdrive.validator.CustomValidator
import java.util.UUID

class ModifyReviewRequest(
    @field:CustomValidator.NotBlank(message = "reviewId는 필수 입력값입니다.")
    val reviewId: UUID,
    @field:CustomValidator.NotBlank(message = "evaluationResultList는 필수 입력값입니다.")
    val evaluationResultList: List<UUID>,
    @field:CustomValidator.NotBlank(message = "score는 필수 입력값입니다.")
//    @field:DecimalMin(
//        value = "0.5",
//        message = "score는 최소 0.5 이상이어야 합니다.",
//    )
//    @field:DecimalMax(
//        value = "5.0",
//        message = "score는 최대 5.0을 초과할 수 없습니다.",
//    )
    val score: Double,
    @field:CustomValidator.NotBlank(message = "content는 필수 입력값입니다.")
    @field:CustomValidator.Size(
        min = 1,
        max = 4000,
        message = "content는 1자 이상 4000자 이하로 작성해주세요.",
    )
    val content: String,
)
