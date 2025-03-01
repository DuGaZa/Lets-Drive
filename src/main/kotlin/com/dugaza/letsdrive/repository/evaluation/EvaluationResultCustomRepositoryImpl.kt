package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.EvaluationResult
import com.dugaza.letsdrive.entity.common.evaluation.QEvaluationResult
import com.querydsl.jpa.impl.JPAQueryFactory
import java.time.LocalDateTime
import java.util.UUID

class EvaluationResultCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : EvaluationResultCustomRepository {
    override fun exists(
        reviewId: UUID,
        questionId: UUID,
        userId: UUID,
    ): Boolean {
        val evaluationResult = QEvaluationResult.evaluationResult
        val fetchFirst =
            jpaQueryFactory
                .selectOne()
                .from(evaluationResult)
                .where(
                    evaluationResult.review.id.eq(reviewId)
                        .and(evaluationResult.answer.question.id.eq(questionId))
                        .and(evaluationResult.user.id.eq(userId)),
                )
                .fetchFirst()

        return fetchFirst != null
    }

    override fun findAll(
        userId: UUID,
        reviewId: UUID,
    ): List<EvaluationResult> {
        val evaluationResult = QEvaluationResult.evaluationResult
        return jpaQueryFactory
            .selectFrom(evaluationResult)
            .where(
                evaluationResult.user.id.eq(userId)
                    .and(evaluationResult.review.id.eq(reviewId)),
            )
            .fetch()
    }

    override fun find(
        userId: UUID,
        reviewId: UUID,
        questionId: UUID,
    ): EvaluationResult? {
        val evaluationResult = QEvaluationResult.evaluationResult
        return jpaQueryFactory
            .selectFrom(evaluationResult)
            .where(
                evaluationResult.user.id.eq(userId)
                    .and(evaluationResult.review.id.eq(reviewId))
                    .and(evaluationResult.answer.question.id.eq(questionId)),
            )
            .fetchOne()
    }

    override fun delete(reviewId: UUID) {
        val evaluationResult = QEvaluationResult.evaluationResult
        jpaQueryFactory
            .update(evaluationResult)
            .set(evaluationResult.deletedAt, LocalDateTime.now())
            .where(evaluationResult.review.id.eq(reviewId))
            .execute()
    }
}
