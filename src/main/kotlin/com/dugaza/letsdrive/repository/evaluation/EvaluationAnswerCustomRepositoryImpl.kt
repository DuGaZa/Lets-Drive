package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import com.dugaza.letsdrive.entity.common.evaluation.QEvaluationAnswer
import com.querydsl.jpa.impl.JPAQueryFactory

class EvaluationAnswerCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : EvaluationAnswerCustomRepository {
    override fun exists(
        question: EvaluationQuestion,
        answer: String,
    ): Boolean {
        val evaluationAnswer = QEvaluationAnswer.evaluationAnswer
        val fetchFirst =
            jpaQueryFactory
                .selectOne()
                .from(evaluationAnswer)
                .where(
                    evaluationAnswer.question.eq(question)
                        .and(evaluationAnswer.answer.eq(answer)),
                )
                .fetchFirst()

        return fetchFirst != null
    }
}
