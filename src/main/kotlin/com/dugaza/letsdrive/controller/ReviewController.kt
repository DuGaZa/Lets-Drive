package com.dugaza.letsdrive.controller

import com.dugaza.letsdrive.dto.review.DeleteReviewRequest
import com.dugaza.letsdrive.dto.review.GetReviewListRequest
import com.dugaza.letsdrive.dto.review.ModifyReviewRequest
import com.dugaza.letsdrive.dto.review.ReviewCreateRequest
import com.dugaza.letsdrive.dto.review.ReviewResponse
import com.dugaza.letsdrive.service.evaluation.EvaluationService
import com.dugaza.letsdrive.service.review.ReviewService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val reviewService: ReviewService,
    private val evaluationService: EvaluationService,
) {
    @PostMapping
    fun registrationReview(
        @RequestBody @Valid request: ReviewCreateRequest,
    ): ResponseEntity<ReviewResponse> {
        val createdReview = reviewService.createReview(request)
        val resultList =
            evaluationService.getEvaluationResultListByReviewId(
                userId = request.userId,
                reviewId = createdReview.id!!,
            )
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                ReviewResponse.of(
                    review = createdReview,
                    evaluationResultList = resultList,
                ),
            )
    }

    @GetMapping
    fun getReviewList(
        @RequestBody @Valid request: GetReviewListRequest,
    ): ResponseEntity<List<ReviewResponse>> {
        return ResponseEntity.ok(
            reviewService.getReviewList(request).map {
                val resultList =
                    evaluationService.getEvaluationResultListByReviewId(
                        userId = it.user.id!!,
                        reviewId = it.id!!,
                    )
                ReviewResponse.of(it, resultList)
            },
        )
    }

    @PatchMapping
    fun modifyReview(
        @RequestBody @Valid request: ModifyReviewRequest,
    ): ResponseEntity<ReviewResponse> {
        val modifiedReview = reviewService.modifyReview(request)
        val resultList =
            evaluationService.getEvaluationResultListByReviewId(
                userId = request.userId,
                reviewId = modifiedReview.id!!,
            )
        return ResponseEntity.ok(
            ReviewResponse.of(
                review = modifiedReview,
                evaluationResultList = resultList,
            ),
        )
    }

    @DeleteMapping
    fun deleteReview(
        @RequestBody @Valid request: DeleteReviewRequest,
    ): ResponseEntity<Unit> {
        reviewService.deleteReview(
            userId = request.userId,
            reviewId = request.reviewId,
        )

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
