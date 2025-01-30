package com.dugaza.letsdrive.dto.review

import com.dugaza.letsdrive.dto.evaluation.EvaluationResultRequest
import com.dugaza.letsdrive.service.TargetType
import com.dugaza.letsdrive.validator.ValidEnum
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

class ReviewCreateDto(
    @field:NotBlank(message = "targetId는 필수 입력값입니다.")
    val targetId: UUID,
    @field:NotBlank(message = "userId는 필수 입력값입니다.")
    val userId: UUID,
    @field:NotBlank(message = "evaluationType은 필수 입력값입니다.")
    val evaluationType: String,
    @field:ValidEnum(
        enumClass = TargetType::class,
        message = "Types are only available for Course.",
        allowNull = false,
        ignoreCase = false,
    )
    val targetType: TargetType,
    @field:NotBlank(message = "evaluationResultList는 필수 입력값입니다.")
    val evaluationResultList: List<EvaluationResultRequest>,
    var files: List<MultipartFile>? = null,
    @field:NotBlank(message = "score는 필수 입력값입니다.")
    @field:DecimalMin(
        value = "0.5",
        message = "score는 최소 0.5 이상이어야 합니다."
    )
    @field:DecimalMax(
        value = "5.0",
        message = "score는 최대 5.0를 초과할 수 없습니다."
    )
    val score: Double,
    @field:NotBlank(message = "content는 필수 입력값입니다.")
    @field:Length(
        min = 1,
        max = 4000,
        message = "content는 1자 이상 4000자 이하로 작성해주세요."
    )
    val content: String,
) {
}