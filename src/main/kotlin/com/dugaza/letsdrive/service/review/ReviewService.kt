package com.dugaza.letsdrive.service.review

import com.dugaza.letsdrive.dto.review.GetReviewListRequest
import com.dugaza.letsdrive.dto.review.ModifyReviewRequest
import com.dugaza.letsdrive.dto.review.ReviewCreateRequest
import com.dugaza.letsdrive.entity.common.Review
import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.user.CustomOAuth2User
import com.dugaza.letsdrive.entity.user.Role
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.extensions.userId
import com.dugaza.letsdrive.repository.review.ReviewRepository
import com.dugaza.letsdrive.service.TargetType
import com.dugaza.letsdrive.service.course.CourseService
import com.dugaza.letsdrive.service.evaluation.EvaluationService
import com.dugaza.letsdrive.service.file.FileService
import com.dugaza.letsdrive.service.user.UserService
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional(readOnly = true)
@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val userService: UserService,
    private val courseService: CourseService,
    private val evaluationService: EvaluationService,
    private val fileService: FileService,
) {
    /**
     * 새로운 Review를 생성하고 저장합니다.
     *
     * @param request Review 생성에 필요한 정보를 담고 있는 DTO (ReviewCreateRequest)
     * @return 저장된 Review Entity
     *
     * 이 함수는 다음과 같은 단계로 Review를 생성합니다:
     * 1. 사용자, 평가, 파일 정보를 검증하고 가져옵니다.
     * 2. 점수의 유효성을 검사합니다.
     * 3. 대상(target)의 존재 여부를 확인합니다.
     * 4. 제공된 평가 질문들의 유효성을 검사합니다.
     * 5. Review 엔티티를 생성하고 저장합니다.
     * 6. 각 평가 결과(EvaluationResult)를 생성합니다.
     *
     * @throws BusinessException 다음과 같은 경우에 발생할 수 있습니다:
     *  - 유효하지 않은 사용자 ID, 평가 ID, 파일 ID
     *  - 유효하지 않은 점수
     *  - 존재하지 않는 대상(target)
     *  - 유효하지 않은 평가 질문
     *
     * @see ReviewCreateRequest
     * @see UserService.getUserById
     * @see EvaluationService.getEvaluationById
     * @see FileService.getFileMaster
     * @see checkValidScore
     * @see checkExistsTarget
     * @see checkValidEvaluationQuestionByAnswerId
     * @see EvaluationService.createEvaluationResult
     *
     * 주의: 이 함수는 @Transactional 어노테이션이 적용되어 있어,
     * 모든 데이터베이스 작업이 하나의 트랜잭션 내에서 실행됩니다.
     */
    @Transactional
    fun createReview(request: ReviewCreateRequest, userId: UUID): Review {
        val targetType = TargetType.valueOf(request.targetType)
        val user = userService.getUserById(userId)
        val evaluation = evaluationService.getEvaluationById(request.evaluationId)
        val fileMaster = fileService.getFileMaster(request.fileMasterId)

        checkValidScore(
            score = request.score,
        )
        checkExistsTarget(
            targetId = request.targetId,
            targetType = targetType,
        )

        request.evaluationResultList.forEach {
            checkValidEvaluationQuestionByAnswerId(
                evaluation = evaluation,
                answerId = it,
            )
        }

        val review =
            reviewRepository.save(
                Review(
                    targetId = request.targetId,
                    user = user,
                    evaluation = evaluation,
                    score = request.score,
                    content = request.content,
                    isDisplayed = true,
                    file = fileMaster,
                ),
            )

        request.evaluationResultList
            .stream()
            .forEach {
                evaluationService.createEvaluationResult(
                    user = user,
                    review = review,
                    answerId = it,
                )
            }

        return review
    }

    /**
     * 기존 리뷰의 정보를 수정합니다.
     *
     * @param request 리뷰 수정에 필요한 정보를 담고 있는 DTO (ModifyReviewRequest)
     * @return 업데이트된 Review Entity
     *
     * 이 함수는 다음과 같은 단계로 리뷰를 수정합니다:
     * 1. 리뷰에 대한 접근권한을 확인합니다.
     * 2. 입력된 점수의 유효성을 검사합니다.
     * 3. 요청된 리뷰 ID로 기존 리뷰를 조회합니다.
     * 4. 요청된 평가 결과 목록의 각 답변을 조회합니다.
     * 5. 리뷰의 점수와 내용을 업데이트합니다.
     * 6. 각 평가 결과(EvaluationResult)를 업데이트합니다.
     *
     * @throws BusinessException 다음과 같은 경우에 발생할 수 있습니다:
     *  - ErrorCode.REVIEW_NOT_FOUND: 유효하지 않은 Review ID일 경우
     *  - ErrorCode.REVIEW_SCORE_INVALID: 유효하지 않은 점수일 경우
     *  - ErrorCode.EVALUATION_ANSWER_NOT_FOUND: 유효하지 않은 Answer ID를 입력했을 경우
     *  - ErrorCode.USER_NOT_FOUND: 유효하지 않은 User ID일 경우
     *  - ErrorCode.EVALUATION_RESULT_NOT_FOUND: 수정하기 전 답변이 존재하지 않을 경우
     *
     * @see ModifyReviewRequest
     * @see checkValidScore
     * @see getReviewById
     * @see EvaluationService.getEvaluationAnswerById
     * @see Review.update
     * @see EvaluationService.updateEvaluationResult
     * @see UserService.getUserById
     *
     * 주의:
     * - 이 함수는 리뷰의 점수와 내용을 직접 수정하고, 연관된 평가 결과도 함께 업데이트합니다.
     * - 이 함수는 @Transactional 어노테이션이 적용되어 있어,
     * 모든 데이터베이스 작업이 하나의 트랜잭션 내에서 실행됩니다.
     * TODO: 리뷰 수정 권한 검사 (로그인 기능 머지 이후 진행)
     */
    @Transactional
    fun modifyReview(
        request: ModifyReviewRequest,
        user: CustomOAuth2User,
    ): Review {
        checkReviewPermission(
            reviewId = request.reviewId,
            user = user,
        )
        checkValidScore(
            score = request.score,
        )
        val review = getReviewById(request.reviewId)

        val answerList =
            request.evaluationResultList.map {
                evaluationService.getEvaluationAnswerById(it)
            }

        review.update(
            score = request.score,
            content = request.content,
        )

        answerList.forEach {
            evaluationService.updateEvaluationResult(
                user = review.user,
                review = review,
                answer = it,
            )
        }

        return reviewRepository.save(review)
    }

    /**
     * 특정 사용자의 리뷰를 삭제합니다.
     *
     * @param userId 리뷰를 삭제하려는 사용자의 UUID
     * @param reviewId 삭제할 리뷰의 UUID
     * @throws BusinessException ErrorCode.REVIEW_NOT_FOUND - 주어진 ID에 해당하는 리뷰가 존재하지 않는 경우
     *
     * 이 함수는 다음과 같은 삭제 과정을 수행합니다:
     * 1. 리뷰의 존재 여부를 확인합니다.
     * 2. 리뷰와 관련된 모든 평가 결과(EvaluationResult)를 삭제합니다.
     * 3. (TODO) 리뷰와 관련된 좋아요 정보를 삭제합니다.
     * 4. 리뷰를 삭제합니다.
     *
     * 주의:
     * - 이 함수는 @Transactional 어노테이션이 적용되어 있어, 모든 삭제 작업이 하나의 트랜잭션 내에서 실행됩니다.
     * - 현재 사용자 권한 확인 로직이 구현되어 있지 않아, 로그인 기능 구현 후 권한 확인 로직을 추가 예정.
     *
     * @see checkExistsReview
     * @see EvaluationService.deleteEvaluationResultByReviewId
     * @see ReviewRepository.deleteById
     *
     * TODO:
     * - 로그인 기능 구현 후 사용자 권한 확인 로직 추가
     * - 리뷰와 관련된 좋아요 정보 삭제 로직 구현
     */
    @Transactional
    fun deleteReview(
        user: CustomOAuth2User,
        reviewId: UUID,
    ) {
        checkExistsReview(reviewId)
        checkReviewPermission(
            reviewId = reviewId,
            user = user
        )
        // 1. evaluationResult 삭제
        evaluationService.deleteEvaluationResultByReviewId(reviewId)
        // 2. review 삭제
        reviewRepository.deleteById(reviewId)
    }

    /**
     * 특정 대상(타겟)에 대한 리뷰 목록을 조회
     *
     * @param request 리뷰 목록 조회 요청 DTO (GetReviewListDto)
     * @return 조회된 리뷰 목록 (List<Review>)
     * @throws BusinessException 다음 경우에 발생:
     *  - 대상(타겟)이 존재하지 않는 경우 (
     *      - target = Course -> ErrorCode.COURSE_NOT_FOUND
     *  )
     *  - 지원하지 않는 TargetType인 경우 (when 표현식이 exhaustive하지 않은 경우)
     */
    fun getReviewList(request: GetReviewListRequest): List<Review> {
        val targetType = TargetType.valueOf(request.targetType)
        checkExistsTarget(
            targetId = request.targetId,
            targetType = targetType,
        )

        return when (targetType) {
            TargetType.COURSE -> reviewRepository.findAllByTargetId(request.targetId)
        }
    }

    /**
     * 주어진 UUID를 사용하여 Review Entity를 조회합니다.
     *
     * @param reviewId 조회할 Review의 UUID
     * @return 조회된 Review Entity
     * @throws BusinessException ErrorCode.REVIEW_NOT_FOUND - 주어진 UUID에 해당하는 Review가 존재하지 않을 경우
     *
     * 이 함수는 다음과 같은 동작을 수행합니다:
     * 1. 주어진 UUID로 reviewRepository에서 Review를 조회합니다.
     * 2. Review가 존재하면 해당 Entity를 반환합니다.
     * 3. Review가 존재하지 않으면 BusinessException을 발생시킵니다.
     *
     * @see ReviewRepository.findById
     * @see BusinessException
     * @see ErrorCode.REVIEW_NOT_FOUND
     *
     * 주의: 이 함수는 null을 반환하지 않습니다. Review가 존재하지 않을 경우 항상 예외를 발생시킵니다.
     */
    fun getReviewById(reviewId: UUID): Review {
        return reviewRepository.findById(reviewId)
            .orElseThrow {
                BusinessException(ErrorCode.REVIEW_NOT_FOUND)
            }
    }

    /**
     * 주어진 TargetID와 TargetType을 사용하여 해당 Entity의 존재 여부를 확인합니다.
     *
     * @param targetId 확인할 대상의 UUID
     * @param targetType 대상의 유형 (TargetType enum)
     * @throws BusinessException 다음과 같은 경우에 발생할 수 있습니다:
     *  - ErrorCode.COURSE_NOT_FOUND: TargetType이 COURSE이고 해당 ID의 Course가 존재하지 않을 경우
     *
     * 이 함수는 다음과 같은 동작을 수행합니다:
     * 1. 주어진 TargetType에 따라 적절한 서비스의 존재 확인 메서드를 호출합니다.
     * 2. 현재는 COURSE 타입만 지원하며, 추후 다른 타입이 추가될 수 있습니다.
     * 3. 대상이 존재하지 않을 경우, 해당 서비스에서 BusinessException을 발생시킵니다.
     *
     * @see TargetType
     * @see CourseService.existsCourseById
     *
     * 주의:
     * - 현재는 COURSE 타입만 지원합니다. 다른 타입에 대한 처리가 필요한 경우 함수를 확장해야 합니다.
     * - 이 함수는 대상이 존재하지 않을 경우 예외를 발생시킵니다.
     */
    fun checkExistsTarget(
        targetId: UUID,
        targetType: TargetType,
    ) {
        when (targetType) {
            TargetType.COURSE -> courseService.existsCourseById(targetId)
        }
    }

    /**
     * 입력된 점수(score)가 유효한 값인지 확인합니다.
     *
     * 유효한 점수의 조건:
     * 1. 0.5 이상 5.0 이하의 값이어야 합니다.
     * 2. 0.5 단위로 입력되어야 합니다. (예: 0.5, 1.0, 1.5, ..., 4.5, 5.0)
     *
     * @param score 검증할 점수 값 (Double)
     * @throws BusinessException ErrorCode.REVIEW_SCORE_INVALID - 점수가 유효하지 않은 경우
     *
     * 이 함수는 다음과 같은 검증을 수행합니다:
     * 1. 점수가 0.5 미만이거나 5.0 초과인 경우 예외를 발생시킵니다.
     * 2. 점수가 0.5의 배수가 아닌 경우 예외를 발생시킵니다.
     *
     * 수학적 검증 방법:
     * - (score * 10).toInt() % 5 != 0 조건을 사용하여 0.5 단위 검사
     *   이 방식은 부동소수점 오차를 피하기 위해 정수 연산을 활용합니다.
     *
     * @see BusinessException
     * @see ErrorCode.REVIEW_SCORE_INVALID
     *
     * 주의:
     * - 이 함수는 점수가 유효하지 않을 경우 예외를 발생시킵니다.
     * - 부동소수점 연산의 특성상, 매우 작은 오차가 발생할 수 있으므로 정수 변환 후 연산합니다.
     */
    fun checkValidScore(score: Double) {
        if (score < 0.5 || score > 5.0 || (score * 10).toInt() % 5 != 0) {
            throw BusinessException(ErrorCode.REVIEW_SCORE_INVALID)
        }
    }

    /**
     * 주어진 답변 ID가 특정 평가에 속하는 질문의 유효한 답변인지 검증합니다.
     *
     * @param evaluation 검증 대상 평가 엔티티 (Evaluation)
     * @param answerId 검증할 답변의 UUID
     * @throws BusinessException 다음과 같은 경우에 발생합니다:
     *  - ErrorCode.EVALUATION_ANSWER_NOT_FOUND: 존재하지 않는 답변 ID인 경우
     *  - ErrorCode.INVALID_EVALUATION_ANSWER: 답변이 주어진 평가에 속하지 않는 경우
     *
     * 이 함수는 다음과 같은 검증 과정을 수행합니다:
     * 1. 주어진 answerId로 EvaluationAnswer 엔티티를 조회합니다.
     * 2. 조회된 답변의 질문이 주어진 evaluation에 속하는지 확인합니다.
     * 3. 속하지 않는 경우 BusinessException을 발생시킵니다.
     *
     * @see Evaluation
     * @see EvaluationService.getEvaluationAnswerById
     * @see BusinessException
     * @see ErrorCode.INVALID_EVALUATION_ANSWER
     *
     * 주의:
     * - 이 함수는 검증에 실패할 경우 예외를 발생시킵니다.
     */
    fun checkValidEvaluationQuestionByAnswerId(
        evaluation: Evaluation,
        answerId: UUID,
    ) {
        val answer = evaluationService.getEvaluationAnswerById(answerId)
        if (answer.question.evaluation.id != evaluation.id) {
            throw BusinessException(ErrorCode.INVALID_EVALUATION_ANSWER)
        }
    }

    /**
     * 주어진 ID에 해당하는 리뷰가 존재하는지 확인합니다.
     *
     * @param reviewId 확인할 리뷰의 UUID
     * @throws BusinessException ErrorCode.REVIEW_NOT_FOUND - 주어진 ID에 해당하는 리뷰가 존재하지 않는 경우
     *
     * 이 함수는 다음과 같은 동작을 수행합니다:
     * 1. 주어진 reviewId로 리뷰의 존재 여부를 확인합니다.
     * 2. 리뷰가 존재하지 않으면 BusinessException을 발생시킵니다.
     *
     * 주의:
     * - 이 함수는 리뷰의 존재 여부만 확인하며, 리뷰 데이터를 반환하지 않습니다.
     *
     * @see ReviewRepository.existsById
     * @see BusinessException
     * @see ErrorCode.REVIEW_NOT_FOUND
     */
    fun checkExistsReview(reviewId: UUID) {
        if (!reviewRepository.existsById(reviewId)) {
            throw BusinessException(ErrorCode.REVIEW_NOT_FOUND)
        }
    }

    fun checkReviewPermission(
        reviewId: UUID,
        user: CustomOAuth2User
    ) {
        val review = this.getReviewById(reviewId)
        if (review.user.id != user.userId && !user.hasRole(Role.ADMIN)) {
            throw BusinessException(ErrorCode.UNAUTHORIZED_ACCESS)
        }
    }
}
