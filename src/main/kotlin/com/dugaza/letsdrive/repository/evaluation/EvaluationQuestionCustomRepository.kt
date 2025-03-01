package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import java.util.UUID

interface EvaluationQuestionCustomRepository {
    fun exists(
        evaluation: Evaluation,
        question: String,
    ): Boolean

    fun findAll(evaluationId: UUID): List<EvaluationQuestion>
}
