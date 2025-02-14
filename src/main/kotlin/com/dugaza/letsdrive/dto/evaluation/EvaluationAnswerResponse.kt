package com.dugaza.letsdrive.dto.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.EvaluationAnswer
import java.util.UUID

class EvaluationAnswerResponse(
    val id: UUID,
    val answer: String,
) {
    companion object {
        fun of(
            evaluationAnswer: EvaluationAnswer,
        ): EvaluationAnswerResponse {
            return EvaluationAnswerResponse(
                id = evaluationAnswer.id!!,
                answer = evaluationAnswer.answer,
            )
        }
    }
}