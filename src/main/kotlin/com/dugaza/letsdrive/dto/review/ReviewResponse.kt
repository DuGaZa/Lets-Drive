package com.dugaza.letsdrive.dto.review

import com.dugaza.letsdrive.dto.evaluation.EvaluationResultResponse
import com.dugaza.letsdrive.entity.common.Review
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationResult
import java.util.UUID

class ReviewResponse(
    val reviewId: UUID,
    val userId: UUID,
    val evaluationResultList: List<EvaluationResultResponse>,
    val score: Double,
    val content: String,
    val isDisplayed: Boolean,
) {
    companion object {
        fun of(
            review: Review,
            evaluationResultList: List<EvaluationResult>,
        ): ReviewResponse {
            return ReviewResponse(
                reviewId = review.id!!,
                userId = review.user.id!!,
                evaluationResultList = evaluationResultList.map {
                    EvaluationResultResponse.of(
                        question = it.answer.question,
                        answer = it.answer,
                    )
                },
                score = review.score,
                content = review.content,
                isDisplayed = review.isDisplayed,
            )
        }
    }
}
