package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.common.evaluation.QEvaluation
import com.querydsl.jpa.impl.JPAQueryFactory
import java.util.UUID

class EvaluationCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : EvaluationCustomRepository {
    override fun find(type: String): Evaluation? {
        val evaluation = QEvaluation.evaluation
        return jpaQueryFactory
            .select(evaluation)
            .where(evaluation.type.eq(type))
            .fetchOne()
    }

    override fun find(id: UUID): Evaluation? {
        val evaluation = QEvaluation.evaluation
        return jpaQueryFactory
            .select(evaluation)
            .where(evaluation.id.eq(id))
            .fetchOne()
    }

    override fun exists(type: String): Boolean {
        val evaluation = QEvaluation.evaluation
        val fetchFirst =
            jpaQueryFactory
                .selectOne()
                .from(evaluation)
                .where(
                    evaluation.type.eq(type),
                )
                .fetchFirst()

        return fetchFirst != null
    }
}
