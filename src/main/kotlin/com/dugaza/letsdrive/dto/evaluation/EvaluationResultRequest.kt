package com.dugaza.letsdrive.dto.evaluation

import jakarta.validation.constraints.NotBlank
import java.util.UUID

class EvaluationResultRequest(
    @field:NotBlank(message = "answerId는 필수 입력값입니다.")
    val answerId: UUID,
) {
}