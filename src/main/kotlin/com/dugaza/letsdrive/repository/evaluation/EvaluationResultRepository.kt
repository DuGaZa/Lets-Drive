package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.Review
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EvaluationResultRepository : JpaRepository<EvaluationResult, UUID> {
    fun existsByReview_IdAndAnswer_Question_IdAndUser_Id(reviewId: UUID, questionId: UUID, userId: UUID): Boolean
    fun findByUser_IdAndReview_IdAndAnswer_Question_Id(userId: UUID, reviewId: UUID, questionId: UUID): Optional<EvaluationResult>
    fun findByUser_IdAndReview_Id(userId: UUID, reviewId: UUID): List<EvaluationResult>
    fun deleteByReviewId(reviewId: UUID)
}