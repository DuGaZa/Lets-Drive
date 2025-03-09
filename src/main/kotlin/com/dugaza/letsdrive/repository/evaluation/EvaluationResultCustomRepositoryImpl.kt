package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.EvaluationResult
import com.dugaza.letsdrive.entity.common.evaluation.QEvaluationResult
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
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

    /**
     * 지정된 리뷰 ID에 해당하는 평가 결과를 삭제합니다.
     *
     * @param reviewId 삭제할 리뷰의 ID
     *
     * @throws BusinessException 존재하지 않는 리뷰 ID일 경우 [ErrorCode.EVALUATION_RESULT_NOT_FOUND] 오류가 발생합니다.
     */
    override fun delete(reviewId: UUID) {
        val evaluationResult = QEvaluationResult.evaluationResult
        val updatedCount = jpaQueryFactory
            .update(evaluationResult)
            .set(evaluationResult.deletedAt, LocalDateTime.now())
            .where(evaluationResult.review.id.eq(reviewId))
            .execute()

        // 위 쿼리에 영향을 받은 행의 수가 0일 경우 Error
        // * 존재하지 않은 Review ID일 경우 *
        if (updatedCount == 0L) {
            throw BusinessException(ErrorCode.EVALUATION_RESULT_NOT_FOUND)
        }
    }
}
