package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EvaluationQuestionRepository : JpaRepository<EvaluationQuestion, UUID> {

    fun existsByEvaluationAndQuestion(evaluation: Evaluation, question: String): Boolean
    fun question(question: String): MutableList<EvaluationQuestion>
    fun findAllByEvaluation_Id(evaluationId: UUID): List<EvaluationQuestion>
}