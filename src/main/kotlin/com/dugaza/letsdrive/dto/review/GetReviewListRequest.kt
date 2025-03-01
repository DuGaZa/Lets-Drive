package com.dugaza.letsdrive.dto.review

import com.dugaza.letsdrive.service.TargetType
import com.dugaza.letsdrive.validator.CustomValidator
import com.dugaza.letsdrive.validator.ValidEnum
import java.util.UUID

class GetReviewListRequest(
    @field:CustomValidator.NotNull(message = "targetId는 필수 입력값입니다.")
    val targetId: UUID,
    @field:ValidEnum(
        enumClass = TargetType::class,
        message = "type은 COURSE 만 가능합니다.",
        allowNull = false,
        ignoreCase = false,
    )
    val targetType: String,
)
