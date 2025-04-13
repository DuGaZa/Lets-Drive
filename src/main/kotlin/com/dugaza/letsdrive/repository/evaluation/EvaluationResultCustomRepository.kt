package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.EvaluationResult
import java.util.UUID

interface EvaluationResultCustomRepository {
    fun exists(
        reviewId: UUID,
        questionId: UUID,
        userId: UUID,
    ): Boolean

    fun find(
        userId: UUID,
        reviewId: UUID,
        questionId: UUID,
    ): EvaluationResult?

    fun findAll(
        userId: UUID,
        reviewId: UUID,
    ): List<EvaluationResult>

    fun delete(reviewId: UUID)
}
