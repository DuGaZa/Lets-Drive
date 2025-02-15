package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.EvaluationAnswer
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EvaluationAnswerRepository : JpaRepository<EvaluationAnswer, UUID>, EvaluationAnswerCustomRepository {
    fun existsByQuestionAndAnswer(
        question: EvaluationQuestion,
        answer: String,
    ): Boolean
}
