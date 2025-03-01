package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.common.evaluation.QEvaluation
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.SimpleExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import java.util.UUID

class EvaluationCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : EvaluationCustomRepository {
    override fun find(
        id: UUID?,
        type: String?,
    ): Evaluation? {
        val evaluation = QEvaluation.evaluation
        return jpaQueryFactory
            .select(evaluation)
            .from(evaluation)
            .where(
                evaluation.id.eqIfNotNull(id),
                evaluation.type.eqIfNotNull(type),
            )
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

    private fun <T> SimpleExpression<T>.eqIfNotNull(value: T?): BooleanExpression? {
        return if (value != null) this.eq(value) else null
    }
}
