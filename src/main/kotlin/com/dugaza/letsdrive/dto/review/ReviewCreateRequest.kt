package com.dugaza.letsdrive.dto.review

import com.dugaza.letsdrive.service.TargetType
import com.dugaza.letsdrive.validator.CustomValidator
import com.dugaza.letsdrive.validator.ValidEnum
import java.util.UUID

class ReviewCreateRequest(
    @field:CustomValidator.NotNull(message = "targetId는 필수 입력값입니다.")
    val targetId: UUID,
    @field:CustomValidator.NotNull(message = "evaluationId은 필수 입력값입니다.")
    val evaluationId: UUID,
    @field:ValidEnum(
        enumClass = TargetType::class,
        message = "Types are only available for Course.",
    )
    val targetType: String,
    @field:CustomValidator.NotNull(message = "evaluationResultList는 필수 입력값입니다.")
    val evaluationResultList: List<UUID>,
    @field:CustomValidator.NotNull(message = "fileMasterId는 필수 입력값입니다.")
    var fileMasterId: UUID,
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
