package com.dugaza.letsdrive.controller

import com.dugaza.letsdrive.dto.review.ModifyReviewRequest
import com.dugaza.letsdrive.dto.review.ReviewResponse
import com.dugaza.letsdrive.entity.user.CustomOAuth2User
import com.dugaza.letsdrive.extensions.userId
import com.dugaza.letsdrive.service.TargetType
import com.dugaza.letsdrive.service.evaluation.EvaluationService
import com.dugaza.letsdrive.service.review.ReviewService
import com.dugaza.letsdrive.vo.review.RegisterReview
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
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
        request: RegisterReview,
        @AuthenticationPrincipal
        user: CustomOAuth2User,
    ): ResponseEntity<ReviewResponse> {
        val createdReview =
            reviewService.createReview(
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
                    profileImageId = createdReview.user.profileImage?.id!!,
                    nickname = createdReview.user.nickname,
                ),
            )
    }

    // MEMO: Spring Boot 3.3 버전 이상부터 Page<> 객체가 도메인 객체로 변경되어
    // 그냥 Page로 JSON 직렬화 시 경고 문구 출력,
    // PagedModel(Spring Boot에서 구현된 Page DTO)로 반환하도록 변경
    // [some link](https://velog.io/@solst_ice/3.3-%EC%9D%B4%EC%83%81-%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B6%80%ED%8A%B8%EC%9D%98-%EC%BB%A8%ED%8A%B8%EB%A1%A4%EB%9F%AC%EC%97%90%EC%84%9C-%EC%97%90%EC%84%9C-Page-%EA%B0%9D%EC%B2%B4%EB%A5%BC-%EA%B7%B8%EB%8C%80%EB%A1%9C-%EC%9D%91%EB%8B%B5%ED%95%A0-%EB%95%8C-%EB%B0%9C%EC%83%9D%ED%95%9C-%EA%B2%BD%EA%B3%A0)
    @GetMapping
    fun getReviewList(
        @RequestParam("targetId")
        targetId: UUID,
        @RequestParam("targetType")
        targetType: TargetType,
        @PageableDefault(
            size = 10,
            page = 0,
            sort = ["createdAt,desc"],
            direction = Sort.Direction.DESC,
        )
        pageable: Pageable,
    ): ResponseEntity<PagedModel<ReviewResponse>> {
        return ResponseEntity.ok(
            reviewService.getReviewList(
                targetId = targetId,
                targetType = targetType,
                pageable = pageable,
            ),
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
                profileImageId = modifiedReview.user.profileImage?.id!!,
                nickname = modifiedReview.user.nickname,
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
