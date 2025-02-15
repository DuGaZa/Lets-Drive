package com.dugaza.letsdrive.service

import com.dugaza.letsdrive.entity.common.Review
import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationAnswer
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationResult
import com.dugaza.letsdrive.entity.user.User
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.repository.evaluation.EvaluationAnswerRepository
import com.dugaza.letsdrive.repository.evaluation.EvaluationQuestionRepository
import com.dugaza.letsdrive.repository.evaluation.EvaluationRepository
import com.dugaza.letsdrive.repository.evaluation.EvaluationResultRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional(readOnly = true)
@Service
class EvaluationService(
    private val evaluationRepository: EvaluationRepository,
    private val evaluationResultRepository: EvaluationResultRepository,
    private val evaluationAnswerRepository: EvaluationAnswerRepository,
    private val evaluationQuestionRepository: EvaluationQuestionRepository,
) {
    /**
     * 새로운 평가 항목을 등록합니다.
     *
     * @param evaluationType 등록할 평가 타입 (문자열)
     * @return 저장된 Evaluation Entity
     * @throws BusinessException ErrorCode.EVALUATION_TYPE_CONFLICT - 이미 존재하는 평가 타입을 등록하려는 경우
     *
     * 이 함수는 다음과 같은 과정을 수행합니다:
     * 1. 주어진 evaluationType이 이미 존재하는지 확인합니다.
     * 2. 중복되지 않은 경우, 새로운 Evaluation 엔티티를 생성하고 저장합니다.
     * 3. 저장된 Evaluation 엔티티를 반환합니다.
     *
     * @see Evaluation
     * @see checkDuplicateType
     * @see EvaluationRepository.save
     *
     * 주의:
     * - 이 함수는 @Transactional 어노테이션이 적용되어 있어, 모든 데이터베이스 작업이 하나의 트랜잭션 내에서 실행됩니다.
     */
    @Transactional
    fun createEvaluation(evaluationType: String): Evaluation {
        checkDuplicateType(evaluationType)

        return evaluationRepository.save(
            Evaluation(evaluationType),
        )
    }

    /**
     * 특정 평가 항목에 대한 새로운 질문을 등록합니다.
     *
     * @param evaluationId 질문을 등록할 평가 항목의 UUID
     * @param question 등록할 질문 내용 (문자열)
     * @return 저장된 EvaluationQuestion Entity
     * @throws BusinessException 다음과 같은 경우에 발생합니다:
     *  - ErrorCode.EVALUATION_NOT_FOUND: 주어진 evaluationId에 해당하는 평가 항목이 존재하지 않는 경우
     *  - ErrorCode.EVALUATION_QUESTION_CONFLICT: 동일한 평가 항목 내에 이미 같은 내용의 질문이 존재하는 경우
     *
     * 이 함수는 다음과 같은 과정을 수행합니다:
     * 1. 주어진 evaluationId로 Evaluation 엔티티를 조회합니다.
     * 2. 해당 Evaluation 내에 동일한 질문이 이미 존재하는지 확인합니다.
     * 3. 중복되지 않은 경우, 새로운 EvaluationQuestion 엔티티를 생성하고 저장합니다.
     * 4. 저장된 EvaluationQuestion 엔티티를 반환합니다.
     *
     * @see Evaluation
     * @see EvaluationQuestion
     * @see getEvaluationById
     * @see checkDuplicateQuestion
     * @see EvaluationQuestionRepository.save
     *
     * 주의:
     * - 이 함수는 @Transactional 어노테이션이 적용되어 있어, 모든 데이터베이스 작업이 하나의 트랜잭션 내에서 실행됩니다.
     */
    @Transactional
    fun createEvaluationQuestion(
        evaluationId: UUID,
        question: String,
    ): EvaluationQuestion {
        val evaluation = getEvaluationById(evaluationId)
        checkDuplicateQuestion(evaluation, question)

        return evaluationQuestionRepository.save(
            EvaluationQuestion(
                evaluation = evaluation,
                question = question,
            ),
        )
    }

    /**
     * 평가 항목에 대한 새로운 답변을 등록합니다.
     *
     * @param questionId 답변을 등록할 평가 항목 질문의 UUID
     * @param answer 등록할 답변 내용 (문자열)
     * @return 저장된 EvaluationAnswer Entity
     * @throws BusinessException 다음과 같은 경우에 발생합니다:
     *  - ErrorCode.EVALUATION_QUESTION_NOT_FOUND: 주어진 questionId에 해당하는 질문이 존재하지 않는 경우
     *  - ErrorCode.EVALUATION_ANSWER_CONFLICT: 동일한 질문에 이미 같은 내용의 답변이 존재하는 경우
     *
     * 이 함수는 다음과 같은 과정을 수행합니다:
     * 1. 주어진 questionId로 EvaluationQuestion 엔티티를 조회합니다.
     * 2. 해당 질문에 동일한 답변이 이미 존재하는지 확인합니다.
     * 3. 중복되지 않은 경우, 새로운 EvaluationAnswer 엔티티를 생성하고 저장합니다.
     * 4. 저장된 EvaluationAnswer 엔티티를 반환합니다.
     *
     * 주의:
     * - 이 함수는 @Transactional 어노테이션이 적용되어 있어, 모든 데이터베이스 작업이 하나의 트랜잭션 내에서 실행됩니다.
     */
    @Transactional
    fun createEvaluationAnswer(
        questionId: UUID,
        answer: String,
    ): EvaluationAnswer {
        val question = getEvaluationQuestionById(questionId)
        checkDuplicateAnswer(question, answer)

        return evaluationAnswerRepository.save(
            EvaluationAnswer(
                question = question,
                answer = answer,
            ),
        )
    }

    /**
     * 새로운 평가 결과를 등록합니다.
     *
     * @param user 평가를 수행한 사용자 Entity
     * @param review 평가 대상 리뷰 Entity
     * @param answerId 선택된 답변의 UUID
     * @return 저장된 EvaluationResult Entity
     * @throws BusinessException ErrorCode.EVALUATION_RESULT_ANSWER_CONFLICT - 동일한 사용자가 같은 리뷰에 대해 이미 답변을 등록한 경우
     *
     * 이 함수는 다음과 같은 과정을 수행합니다:
     * 1. 주어진 answerId로 EvaluationAnswer 엔티티를 조회합니다.
     * 2. 동일한 사용자가 같은 리뷰에 대해 이미 답변을 등록했는지 확인합니다.
     * 3. 중복되지 않은 경우, 새로운 EvaluationResult 엔티티를 생성하고 저장합니다.
     * 4. 저장된 EvaluationResult 엔티티를 반환합니다.
     *
     * 주의:
     * - 이 함수는 @Transactional 어노테이션이 적용되어 있어, 모든 데이터베이스 작업이 하나의 트랜잭션 내에서 실행됩니다.
     */
    @Transactional
    fun createEvaluationResult(
        user: User,
        review: Review,
        answerId: UUID,
    ): EvaluationResult {
        val answer = getEvaluationAnswerById(answerId)
        checkDuplicateResultAnswer(review, user, answer)

        return evaluationResultRepository.save(
            EvaluationResult(
                review = review,
                answer = answer,
                user = user,
            ),
        )
    }

    /**
     * EvaluationResult를 업데이트합니다.
     * @param user 사용자 Entity
     * @param review 리뷰 Entity
     * @param answer 평가 답변 Entity
     * @return 업데이트된 EvaluationResult Entity
     */
    @Transactional
    fun updateEvaluationResult(
        user: User,
        review: Review,
        answer: EvaluationAnswer,
    ): EvaluationResult {
        val evaluationResult =
            getEvaluationResultByQuestionId(
                userId = user.id!!,
                reviewId = review.id!!,
                questionId = answer.question.id!!,
            )

        evaluationResult.answer = answer
        return evaluationResultRepository.save(evaluationResult)
    }

    /**
     * 특정 리뷰 ID에 해당하는 모든 평가 결과를 삭제합니다.
     *
     * @param reviewId 삭제할 평가 결과들과 연관된 리뷰의 UUID
     *
     * 이 함수는 다음과 같은 동작을 수행합니다:
     * 1. 주어진 reviewId와 연관된 모든 EvaluationResult 엔티티를 찾습니다.
     * 2. 찾은 모든 EvaluationResult 엔티티를 데이터베이스에서 삭제합니다.
     *
     * 주의:
     * - 이 함수는 @Transactional 어노테이션이 적용되어 있어, 모든 삭제 작업이 하나의 트랜잭션 내에서 실행됩니다.
     *
     * @see EvaluationResult
     * @see EvaluationResultRepository.delete
     */
    @Transactional
    fun deleteEvaluationResultByReviewId(reviewId: UUID) {
        evaluationResultRepository.delete(
            reviewId = reviewId,
        )
    }

    /**
     * UUID로 EvaluationAnswer Entity를 조회합니다.
     * @param answerId EvaluationAnswer UUID
     * @return 조회된 EvaluationAnswer Entity
     * @throws BusinessException ErrorCode.EVALUATION_ANSWER_NOT_FOUND - 답변을 찾을 수 없는 경우
     */
    fun getEvaluationAnswerById(answerId: UUID): EvaluationAnswer {
        return evaluationAnswerRepository.findById(answerId)
            .orElseThrow {
                BusinessException(ErrorCode.EVALUATION_ANSWER_NOT_FOUND)
            }
    }

    /**
     * UUID로 Evaluation Entity를 조회합니다.
     * @param evaluationId 평가 UUID
     * @return Evaluation Entity
     * @throws BusinessException ErrorCode.EVALUATION_NOT_FOUND - 평가를 찾을 수 없는 경우
     */
    fun getEvaluationById(evaluationId: UUID): Evaluation {
        return evaluationRepository.findById(evaluationId)
            .orElseThrow {
                BusinessException(ErrorCode.EVALUATION_NOT_FOUND)
            }
    }

    /**
     * 평가 유형으로 Evaluation Entity를 조회합니다.
     * @param evaluationType Evaluation Type
     * @return Evaluation Entity
     * @throws BusinessException ErrorCode.EVALUATION_NOT_FOUND - 평가를 찾을 수 없는 경우
     */
    fun getEvaluationByType(evaluationType: String): Evaluation {
        return evaluationRepository.find(
            type = evaluationType,
        ) ?: throw BusinessException(ErrorCode.EVALUATION_NOT_FOUND)
    }

    /**
     * UUID로 EvaluationQuestion Entity를 조회합니다.
     * @param questionId 평가 항목 질문 UUID
     * @return EvaluationQuestion Entity
     * @throws BusinessException ErrorCode.EVALUATION_QUESTION_NOT_FOUND - 질문을 찾을 수 없는 경우
     */
    fun getEvaluationQuestionById(questionId: UUID): EvaluationQuestion {
        return evaluationQuestionRepository.findById(questionId)
            .orElseThrow {
                BusinessException(ErrorCode.EVALUATION_QUESTION_NOT_FOUND)
            }
    }

    /**
     * 평가 ID로 EvaluationQuestion Entity 목록을 조회합니다.
     * @param evaluationId 평가 UUID
     * @return EvaluationQuestion Entity 목록
     */
    fun getEvaluationQuestionListByEvaluationId(evaluationId: UUID): List<EvaluationQuestion> {
        return evaluationQuestionRepository.findAll(
            evaluationId = evaluationId,
        )
    }

    /**
     * 사용자 ID, 리뷰 ID, 질문 ID로 EvaluationResult Entity를 조회합니다.
     * @param userId 사용자 UUID
     * @param reviewId 리뷰 UUID
     * @param questionId 질문 UUID
     * @return EvaluationResult Entity
     * @throws BusinessException ErrorCode.EVALUATION_RESULT_NOT_FOUND - 결과를 찾을 수 없는 경우
     */
    fun getEvaluationResultByQuestionId(
        userId: UUID,
        reviewId: UUID,
        questionId: UUID,
    ): EvaluationResult {
        return evaluationResultRepository.find(
            userId = userId,
            reviewId = reviewId,
            questionId = questionId,
        ) ?: throw BusinessException(ErrorCode.EVALUATION_RESULT_NOT_FOUND)
    }

    /**
     * 사용자 ID와 리뷰 ID로 EvaluationResult Entity 목록을 조회합니다.
     * @param userId 사용자 UUID
     * @param reviewId 리뷰 UUID
     * @return EvaluationResult Entity 목록
     */
    fun getEvaluationResultListByReviewId(
        userId: UUID,
        reviewId: UUID,
    ): List<EvaluationResult> {
        return evaluationResultRepository.findAll(
            userId = userId,
            reviewId = reviewId,
        )
    }

    /**
     * 동일한 리뷰에 대해 같은 질문에 대한 답변이 이미 존재하는지 확인합니다.
     * @param review 리뷰 Entity
     * @param user 사용자 Entity
     * @param answer 평가 답변 Entity
     * @throws BusinessException ErrorCode.EVALUATION_RESULT_ANSWER_CONFLICT - 중복된 답변이 존재하는 경우
     */
    fun checkDuplicateResultAnswer(
        review: Review,
        user: User,
        answer: EvaluationAnswer,
    ) {
        if (evaluationResultRepository.exists(
                reviewId = review.id!!,
                questionId = answer.question.id!!,
                userId = user.id!!,
            )
        ) {
            throw BusinessException(ErrorCode.EVALUATION_RESULT_ANSWER_CONFLICT)
        }
    }

    /**
     * 동일한 평가 내에 중복된 질문이 있는지 확인합니다.
     * @param evaluation 평가 Entity
     * @param question 질문 문자열
     * @throws BusinessException ErrorCode.EVALUATION_QUESTION_CONFLICT - 중복된 질문이 존재하는 경우
     */
    fun checkDuplicateQuestion(
        evaluation: Evaluation,
        question: String,
    ) {
        if (evaluationQuestionRepository.exists(
                evaluation = evaluation,
                question = question,
            )
        ) {
            throw BusinessException(ErrorCode.EVALUATION_QUESTION_CONFLICT)
        }
    }

    /**
     * 동일한 질문에 대해 중복된 답변이 있는지 확인합니다.
     * @param question 평가 질문 Entity
     * @param answer 답변 문자열
     * @throws BusinessException ErrorCode.EVALUATION_ANSWER_CONFLICT - 중복된 답변이 존재하는 경우
     */
    fun checkDuplicateAnswer(
        question: EvaluationQuestion,
        answer: String,
    ) {
        if (evaluationAnswerRepository.existsByQuestionAndAnswer(question, answer)) {
            throw BusinessException(ErrorCode.EVALUATION_ANSWER_CONFLICT)
        }
    }

    /**
     * 중복된 평가 유형이 있는지 확인합니다.
     * @param evaluationType 평가 유형 문자열
     * @throws BusinessException ErrorCode.EVALUATION_TYPE_CONFLICT - 중복된 평가 유형이 존재하는 경우
     */
    fun checkDuplicateType(evaluationType: String) {
        if (evaluationRepository.exists(
                type = evaluationType,
            )
        ) {
            throw BusinessException(ErrorCode.EVALUATION_TYPE_CONFLICT)
        }
    }
}
