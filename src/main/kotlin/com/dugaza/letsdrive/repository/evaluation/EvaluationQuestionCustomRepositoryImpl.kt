package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import com.dugaza.letsdrive.entity.common.evaluation.QEvaluationQuestion
import com.querydsl.jpa.impl.JPAQueryFactory
import java.util.UUID

class EvaluationQuestionCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : EvaluationQuestionCustomRepository {
    override fun exists(
        evaluation: Evaluation,
        question: String,
    ): Boolean {
        val evaluationQuestion = QEvaluationQuestion.evaluationQuestion
        val fetchFirst =
            jpaQueryFactory
                .selectOne()
                .from(evaluationQuestion)
                .where(
                    evaluationQuestion.evaluation.eq(evaluation)
                        .and(evaluationQuestion.question.eq(question)),
                )
                .fetchFirst()

        return fetchFirst != null
    }

    override fun findAll(evaluationId: UUID): List<EvaluationQuestion> {
        val evaluationQuestion = QEvaluationQuestion.evaluationQuestion
        return jpaQueryFactory
            .selectFrom(evaluationQuestion)
            .where(
                evaluationQuestion.evaluation.id.eq(evaluationId),
            )
            .fetch()
    }
}
