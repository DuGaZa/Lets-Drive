package com.dugaza.letsdrive.dto.review

import com.dugaza.letsdrive.entity.common.Review
import java.util.UUID

data class ReviewResponse(
    val reviewId: UUID,
    val userId: UUID,
    val score: Double,
    val content: String,
    val isDisplayed: Boolean,
) {
    companion object {
        fun of(
            review: Review
        ) : ReviewResponse {
            return ReviewResponse(
                reviewId = review.id!!,
                userId = review.user.id!!,
                score = review.score,
                content = review.content,
                isDisplayed = review.isDisplayed
            )
        }
    }
}
