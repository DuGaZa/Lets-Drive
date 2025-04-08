package com.dugaza.letsdrive.dto.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.EvaluationAnswer
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion

class EvaluationResultResponse(
    val question: EvaluationQuestionResponse,
    val answer: EvaluationAnswerResponse,
) {
    companion object {
        fun of(
            question: EvaluationQuestion,
            answer: EvaluationAnswer,
        ): EvaluationResultResponse {
            return EvaluationResultResponse(
                question = EvaluationQuestionResponse.of(question),
                answer = EvaluationAnswerResponse.of(answer),
            )
        }
    }
}
