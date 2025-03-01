package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion

interface EvaluationAnswerCustomRepository {
    fun exists(
        question: EvaluationQuestion,
        answer: String,
    ): Boolean
}
