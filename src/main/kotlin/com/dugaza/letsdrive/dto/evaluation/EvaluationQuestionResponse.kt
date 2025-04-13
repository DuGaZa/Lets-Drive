package com.dugaza.letsdrive.dto.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import java.util.UUID

class EvaluationQuestionResponse(
    val id: UUID,
    val question: String,
) {
    companion object {
        fun of(evaluationQuestion: EvaluationQuestion): EvaluationQuestionResponse {
            return EvaluationQuestionResponse(
                id = evaluationQuestion.id!!,
                question = evaluationQuestion.question,
            )
        }
    }
}
