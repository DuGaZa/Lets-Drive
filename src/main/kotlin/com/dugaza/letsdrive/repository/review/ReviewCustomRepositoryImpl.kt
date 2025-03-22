package com.dugaza.letsdrive.repository.review

import com.dugaza.letsdrive.dto.evaluation.EvaluationAnswerResponse
import com.dugaza.letsdrive.dto.evaluation.EvaluationQuestionResponse
import com.dugaza.letsdrive.dto.evaluation.EvaluationResultResponse
import com.dugaza.letsdrive.dto.review.ReviewResponse
import com.dugaza.letsdrive.entity.common.QReview
import com.dugaza.letsdrive.entity.common.Review
import com.dugaza.letsdrive.entity.common.evaluation.QEvaluationAnswer
import com.dugaza.letsdrive.entity.common.evaluation.QEvaluationQuestion
import com.dugaza.letsdrive.entity.common.evaluation.QEvaluationResult
import com.dugaza.letsdrive.entity.user.QUser
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.data.web.PagedModel
import java.time.LocalDateTime
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class ReviewCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ReviewCustomRepository {
    override fun findAllByTargetIdWithPage(
        targetId: UUID,
        pageable: Pageable,
    ): PagedModel<ReviewResponse> {
        val review = QReview.review
        val user = QUser.user
        val evaluationQuestion = QEvaluationQuestion.evaluationQuestion
        val evaluationAnswer = QEvaluationAnswer.evaluationAnswer
        val evaluationResult = QEvaluationResult.evaluationResult
        val count = getCount(targetId)
        val result = jpaQueryFactory
            .select(
                Projections.constructor(
                    ReviewResponse::class.java,
                    review.id,
                    user.id,
                    user.profileImage.id,
                    user.nickname,
                    Projections.list(
                        Projections.constructor(
                            EvaluationResultResponse::class.java,
                            Projections.constructor(
                                EvaluationQuestionResponse::class.java,
                                evaluationQuestion.id,
                                evaluationQuestion.question,
                            ),
                            Projections.constructor(
                                EvaluationAnswerResponse::class.java,
                                evaluationAnswer.id,
                                evaluationAnswer.answer,
                            ),
                        ),
                    ),
                    review.score,
                    review.content,
                    review.createdAt,
                )
            )
            .from(review)
            .join(review.user, user)
            .leftJoin(evaluationResult).on(review.id.eq(evaluationResult.review.id))
            .join(evaluationResult.answer, evaluationAnswer)//
            .join(evaluationAnswer.question, evaluationQuestion)
            .where(
                review.targetId.eq(targetId)
                    .and(review.isDisplayed.isTrue)
                    .and(review.deletedAt.isNull)
            )
            .orderBy(*getOrderSpecifiers(pageable.sort).toTypedArray()) // !
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val processedResult = result.groupBy { it.reviewId }.map {
            (_, reviewList) ->
            val firstReview = reviewList.first()
            val evaluationResultList = reviewList.flatMap { it.evaluationResultList }
            ReviewResponse(
                reviewId = firstReview.reviewId,
                userId = firstReview.userId,
                profileImageId = firstReview.profileImageId,
                nickname = firstReview.nickname,
                evaluationResultList = evaluationResultList,
                score = firstReview.score,
                content = firstReview.content,
                createdAt = firstReview.createdAt,
            )
        }

        return PagedModel(PageableExecutionUtils.getPage(processedResult, pageable) { count })
    }

    private fun getCount(
        targetId: UUID,
    ): Long {
        val review = QReview.review
        return jpaQueryFactory
            .select(review.count())
            .from(review)
            .where(
                review.targetId.eq(targetId)
            )
            .fetchOne() ?: 0L
    }

    private fun getOrderSpecifiers(sort: Sort): List<OrderSpecifier<*>> {
        return sort.map { order ->
            val direction = if (order.isAscending) Order.ASC else Order.DESC
            val propertyNames = getAllPropertyNames(Review::class)
            propertyNames.contains(order.property).takeIf { it }?.let {
                when (order.property) {
                    "createdAt" -> OrderSpecifier(
                        direction,
                        Expressions.path(LocalDateTime::class.java, QReview.review, order.property)
                    )
                    "score" -> OrderSpecifier(
                        direction,
                        Expressions.path(Double::class.java, QReview.review, order.property)
                    )
                    else -> throw BusinessException(ErrorCode.DEFAULT_VALIDATION_FAILED)
                }
            }
        }.filterNotNull().toList()
    }

    private fun getAllPropertyNames(clazz: KClass<*>): List<String> {
        return clazz.memberProperties.map { it.name }
    }
}