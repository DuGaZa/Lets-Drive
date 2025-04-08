package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import com.dugaza.letsdrive.entity.common.evaluation.QEvaluationAnswer
import com.dugaza.letsdrive.repository.common.Checks
import com.dugaza.letsdrive.repository.common.Checks.eqIfNotNull
import com.querydsl.jpa.impl.JPAQueryFactory

class EvaluationAnswerCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : EvaluationAnswerCustomRepository {
    override fun exists(
        question: EvaluationQuestion?,
        answer: String?,
    ): Boolean {
        Checks.argsIsNotNull(question, answer)

        val evaluationAnswer = QEvaluationAnswer.evaluationAnswer
        val fetchFirst =
            jpaQueryFactory
                .selectOne()
                .from(evaluationAnswer)
                .where(
                    evaluationAnswer.question.eqIfNotNull(question),
                    evaluationAnswer.answer.eqIfNotNull(answer),
                )
                .fetchFirst()

        return fetchFirst != null
    }
}
