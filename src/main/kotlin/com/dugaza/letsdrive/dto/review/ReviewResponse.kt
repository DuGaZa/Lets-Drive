package com.dugaza.letsdrive.dto.review

import com.dugaza.letsdrive.dto.evaluation.EvaluationResultResponse
import com.dugaza.letsdrive.entity.common.Review
import java.time.LocalDateTime
import java.util.UUID

class ReviewResponse(
    val reviewId: UUID,
    val userId: UUID,
    val profileImageId: UUID,
    val nickname: String,
    val evaluationResultList: List<EvaluationResultResponse>,
    val score: Double,
    val content: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun of(
            review: Review,
            evaluationResultList: List<EvaluationResultResponse>,
            profileImageId: UUID,
            nickname: String,
        ): ReviewResponse {
            return ReviewResponse(
                reviewId = review.id!!,
                userId = review.user.id!!,
                profileImageId = profileImageId,
                nickname = nickname,
                evaluationResultList = evaluationResultList,
                score = review.score,
                content = review.content,
                createdAt = review.createdAt!!,
            )
        }
    }
}
