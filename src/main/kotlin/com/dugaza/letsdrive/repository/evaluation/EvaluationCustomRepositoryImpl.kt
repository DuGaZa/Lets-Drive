package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.common.evaluation.QEvaluation
import com.dugaza.letsdrive.repository.common.Checks
import com.dugaza.letsdrive.repository.common.Checks.eqIfNotNull
import com.querydsl.jpa.impl.JPAQueryFactory
import java.util.UUID

class EvaluationCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : EvaluationCustomRepository {
    override fun find(
        id: UUID?,
        type: String?,
    ): Evaluation? {
        Checks.argsIsNotNull(id, type)

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
}
