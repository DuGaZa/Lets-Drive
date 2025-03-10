package com.dugaza.letsdrive.controller

import com.dugaza.letsdrive.dto.review.ModifyReviewRequest
import com.dugaza.letsdrive.dto.review.ReviewCreateRequest
import com.dugaza.letsdrive.dto.review.ReviewResponse
import com.dugaza.letsdrive.entity.user.CustomOAuth2User
import com.dugaza.letsdrive.extensions.userId
import com.dugaza.letsdrive.service.TargetType
import com.dugaza.letsdrive.service.evaluation.EvaluationService
import com.dugaza.letsdrive.service.review.ReviewService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val reviewService: ReviewService,
    private val evaluationService: EvaluationService,
) {
    @PostMapping
    fun registerReview(
        @RequestBody
        @Valid
        request: ReviewCreateRequest,
        @AuthenticationPrincipal
        user: CustomOAuth2User,
    ): ResponseEntity<ReviewResponse> {
        val createdReview = reviewService.createReview(
            targetId = request.targetId,
            targetType = request.targetType,
            evaluationId = request.evaluationId,
            evaluationResultList = request.evaluationResultList,
            fileMasterId = request.fileMasterId,
            score = request.score,
            content = request.content,
            userId = user.userId,
        )
        val resultList =
            evaluationService.getEvaluationResultListByReviewId(
                userId = user.userId,
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
        @RequestParam("targetId")
        targetId: UUID,
        @RequestParam("targetType")
        targetType: TargetType,
        @PageableDefault(
            size = 10,
            page = 0,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC,
        )
        pageable: Pageable,
    ): ResponseEntity<Page<ReviewResponse>> {
        return ResponseEntity.ok(
            reviewService.getReviewList(
                targetId = targetId,
                targetType = targetType,
            ).map {
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
        @RequestBody
        @Valid
        request: ModifyReviewRequest,
        @AuthenticationPrincipal
        user: CustomOAuth2User,
    ): ResponseEntity<ReviewResponse> {
        val modifiedReview =
            reviewService.modifyReview(
                request = request,
                user = user,
            )
        val resultList =
            evaluationService.getEvaluationResultListByReviewId(
                userId = user.userId,
                reviewId = modifiedReview.id!!,
            )
        return ResponseEntity.ok(
            ReviewResponse.of(
                review = modifiedReview,
                evaluationResultList = resultList,
            ),
        )
    }

    @DeleteMapping("/{reviewId}")
    fun deleteReview(
        @PathVariable
        reviewId: UUID,
        @AuthenticationPrincipal
        user: CustomOAuth2User,
    ): ResponseEntity<Unit> {
        reviewService.deleteReview(
            user = user,
            reviewId = reviewId,
        )

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
