package com.dugaza.letsdrive.service

import com.dugaza.letsdrive.dto.evaluation.EvaluationResultRequest
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
     * 평가 항목 등록
     *
     * 이미 저장되어있는 evaluation type을 저장할 경우 exception 발생
     * @param evaluationType 평가 타입
     * @return 저장된 Evaluation Entity
     * @exception BusinessException ErrorCode.EVALUATION_TYPE_CONFLICT
     */
    fun createEvaluation(
        evaluationType: String
    ) : Evaluation {
        checkDuplicateType(evaluationType)

        return evaluationRepository.save(
            Evaluation(evaluationType)
        )
    }

    /**
     * 평가 항목 질문 등록
     *
     * 한 Evaluatoin에 같은 이름의 question을 저장할 경우 exception 발생
     * @param evaluationId 평가 항목 UUID
     * @param question 질문
     * @return 저장된 EvaluationQuestion Entity
     * @exception BusinessException ErrorCode.EVALUATION_QUESTION_CONFLICT
     */
    fun createEvaluationQuestion(
        evaluationId: UUID,
        question: String
    ) : EvaluationQuestion {
        val evaluation = getEvaluationById(evaluationId)
        checkDuplicateQuestion(evaluation, question)

        return evaluationQuestionRepository.save(
            EvaluationQuestion(
                evaluation = evaluation,
                question = question,
            )
        )
    }

    /**
     * 평가 항목 답변 등록
     *
     * Question에 같은 이름의 answer 저장할 경우 exception 발생
     * @param questionId 평가 항목 질문 UUID
     * @param answer 답변
     * @return 저장된 EvaluationAnswer Entity
     * @exception BusinessException ErrorCode.EVALUATION_ANSWER_CONFLICT
     */
    fun createEvaluationAnswer(
        questionId: UUID,
        answer: String
    ) : EvaluationAnswer {
        val question = getEvaluationQuestionById(questionId)
        checkDuplicateAnswer(question, answer)

        return evaluationAnswerRepository.save(
            EvaluationAnswer(
                question = question,
                answer = answer
            )
        )
    }

    /**
     * 평가 결과 등록
     * @param userId 사용자 UUID
     * @param evaluationResult 평가 결과 등록 DTO
     * @return 저장된 EvaluationResult Entity
     */
    fun createEvaluationResult(
        user: User,
        review: Review,
        evaluationResult: EvaluationResultRequest
    ): EvaluationResult {
        val answer = getEvaluationAnswerById(evaluationResult.answerId)

        return evaluationResultRepository.save(
            EvaluationResult(
                review = review,
                answer = answer,
                user = user,
            )
        )
    }

    /**
     * EvaluationAnswer UUID를 이용하여 EvaluationAnswer Entity 조회
     * @param answerId EvaluationAnswer UUID
     * @return 조회된 EvaluationAnswer Entity
     * @exception BusinessException ErrorCode.EVALUATION_ANSWER_NOT_FOUND
     */
    fun getEvaluationAnswerById(
        answerId: UUID,
    ): EvaluationAnswer {
        return evaluationAnswerRepository.findById(answerId)
            .orElseThrow {
                BusinessException(ErrorCode.EVALUATION_ANSWER_NOT_FOUND)
            }
    }

    /**
     * Evaluation UUID를 이용하여 Evaluation Entity 조회
     * @param evaluationId 평가 UUID
     * @return Evaluation Entity
     */
    fun getEvaluationById(
        evaluationId: UUID
    ): Evaluation {
        return evaluationRepository.findById(evaluationId)
            .orElseThrow {
                BusinessException(ErrorCode.EVALUATION_NOT_FOUND)
            }
    }

    /**
     * Evaluation Type을 이용하여 Evaluation Entity 조회
     * @param evaluationType Evaluation Type
     * @return Evaluation Entity
     */
    fun getEvaluationByType(
        evaluationType: String
    ): Evaluation {
        return evaluationRepository.findByType(evaluationType)
            .orElseThrow {
                BusinessException(ErrorCode.EVALUATION_NOT_FOUND)
            }
    }

    /**
     * Question ID를 이용하여 EvaluationQuestion Entity 조회
     * @param questionId 평가 항목 질문 UUID
     * @return EvaluationQuestion Entity
     * @exception BusinessException ErrorCode.EVALUATION_QUESTION_NOT_FOUND
     */
    fun getEvaluationQuestionById(
        questionId: UUID
    ) : EvaluationQuestion {
        return evaluationQuestionRepository.findById(questionId)
            .orElseThrow {
                BusinessException(ErrorCode.EVALUATION_QUESTION_NOT_FOUND)
            }
    }

    private fun checkDuplicateQuestion(
        evaluation: Evaluation,
        question: String
    ) {
        if (evaluationQuestionRepository.existsByEvaluationAndQuestion(evaluation, question)) {
            throw BusinessException(ErrorCode.EVALUATION_QUESTION_CONFLICT)
        }
    }

    private fun checkDuplicateAnswer(
        question: EvaluationQuestion,
        answer: String
    ) {
        if (evaluationAnswerRepository.existsByQuestionAndAnswer(question, answer)) {
            throw BusinessException(ErrorCode.EVALUATION_ANSWER_CONFLICT)
        }
    }

    private fun checkDuplicateType(
        evaluationType: String
    ) {
        if (evaluationRepository.existsByType(evaluationType)) {
            throw BusinessException(ErrorCode.EVALUATION_TYPE_CONFLICT)
        }
    }
}