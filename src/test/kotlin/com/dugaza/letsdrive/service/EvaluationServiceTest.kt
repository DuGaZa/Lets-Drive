package com.dugaza.letsdrive.service

import com.dugaza.letsdrive.entity.common.Review
import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationAnswer
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationResult
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationType
import com.dugaza.letsdrive.entity.user.AuthProvider
import com.dugaza.letsdrive.entity.user.User
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.repository.evaluation.EvaluationAnswerRepository
import com.dugaza.letsdrive.repository.evaluation.EvaluationQuestionRepository
import com.dugaza.letsdrive.repository.evaluation.EvaluationRepository
import com.dugaza.letsdrive.repository.evaluation.EvaluationResultRepository
import com.dugaza.letsdrive.service.evaluation.EvaluationService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import java.util.UUID

@ExtendWith(MockKExtension::class)
class EvaluationServiceTest {
    @MockK
    private lateinit var evaluationRepository: EvaluationRepository

    @MockK
    private lateinit var evaluationQuestionRepository: EvaluationQuestionRepository

    @MockK
    private lateinit var evaluationAnswerRepository: EvaluationAnswerRepository

    @MockK
    private lateinit var evaluationResultRepository: EvaluationResultRepository

    @InjectMockKs
    private lateinit var evaluationService: EvaluationService

    private lateinit var mockEvaluation: Evaluation
    private lateinit var evaluationId: UUID
    private lateinit var mockEvaluationAnswer: EvaluationAnswer
    private lateinit var evaluationAnswerId: UUID
    private lateinit var mockEvaluationQuestion: EvaluationQuestion
    private lateinit var evaluationQuestionId: UUID
    private lateinit var mockUser: User
    private lateinit var userId: UUID
    private lateinit var userProviderId: String
    private lateinit var mockReview: Review
    private lateinit var reviewId: UUID

    private val evaluationType = EvaluationType.CUSTOM
    private val evaluationQuestionContent = "Test Evaluation Question"
    private val evaluationAnswerContent = "Test Evaluation Answer"
    private val userEmail = "mock@example.com"

    @BeforeEach
    fun setUp() {
        evaluationId = UUID.randomUUID()
        evaluationAnswerId = UUID.randomUUID()
        evaluationQuestionId = UUID.randomUUID()
        userId = UUID.randomUUID()
        userProviderId = UUID.randomUUID().toString()
        reviewId = UUID.randomUUID()

        mockEvaluation =
            mockk<Evaluation> {
                every { id } returns evaluationId
                every { type } returns evaluationType
            }

        mockEvaluationQuestion =
            mockk<EvaluationQuestion> {
                every { id } returns evaluationQuestionId
                every { evaluation } returns mockEvaluation
                every { question } returns evaluationQuestionContent
            }

        mockEvaluationAnswer =
            mockk<EvaluationAnswer> {
                every { id } returns evaluationAnswerId
                every { question } returns mockEvaluationQuestion
                every { answer } returns evaluationAnswerContent
            }

        mockUser =
            mockk<User> {
                every { id } returns userId
                every { email } returns userEmail
                every { provider } returns AuthProvider.GOOGLE
                every { providerId } returns userProviderId
                every { nickname } returns "TEST_USER_NICKNAME"
            }

        mockReview =
            mockk<Review> {
                every { id } returns reviewId
                every { targetId } returns UUID.randomUUID()
                every { user } returns mockUser
                every { evaluation } returns mockEvaluation
                every { score } returns 0.5
                every { content } returns "TEST_REVIEW_CONTENT"
                every { isDisplayed } returns true
            }
    }

    @Nested
    @DisplayName("checkDuplicateType(): ")
    inner class CheckDuplicateType {
        @Test
        @DisplayName("중복 평가 타입 체크 시 예외 발생 테스트")
        fun `checking duplicate evaluation type should throw exception`() {
            // Given
            every {
                evaluationRepository.exists(
                    type = evaluationType,
                )
            } returns true

            // When
            // 같은 Evaluation Type이 있을 경우 Exception
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.checkDuplicateType(evaluationType)
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_TYPE_CONFLICT, exception.errorCode)
        }

        @Test
        @DisplayName("중복되지 않은 평가 타입 체크 테스트")
        fun `checking non-duplicate evaluation type should not throw exception`() {
            // Given
            every { evaluationRepository.exists(type = any()) } returns false

            // When & Then
            assertDoesNotThrow {
                evaluationService.checkDuplicateType(EvaluationType.MANAGER)
            }
        }
    }

    @Nested
    @DisplayName("checkDuplicateAnswer(): ")
    inner class CheckDuplicateAnswer {
        @Test
        @DisplayName("중복 평가 답변 체크 시 예외 발생 테스트")
        fun `checking duplicate evaluation answer should throw exception`() {
            // Given
            every {
                evaluationAnswerRepository.existsByQuestionAndAnswer(
                    question = mockEvaluationQuestion,
                    answer = evaluationAnswerContent,
                )
            } returns true

            // When
            // 같은 Evaluation Question에 중복된 Answer를 만드려고 할 시 Exception
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.checkDuplicateAnswer(
                        question = mockEvaluationQuestion,
                        answer = evaluationAnswerContent,
                    )
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_ANSWER_CONFLICT, exception.errorCode)
        }

        @Test
        @DisplayName("중복되지 않은 평가 타입 체크 테스트")
        fun `checking non-duplicate evaluation answer should not throw exception`() {
            // Given
            every {
                evaluationAnswerRepository.existsByQuestionAndAnswer(
                    question = mockEvaluationQuestion,
                    answer = any(),
                )
            } returns false

            // When & Then
            assertDoesNotThrow {
                evaluationService.checkDuplicateAnswer(
                    question = mockEvaluationQuestion,
                    answer = "NEW_ANSWER",
                )
            }
        }
    }

    @Nested
    @DisplayName("checkDuplicateQuestion(): ")
    inner class CheckDuplicateQuestion {
        @Test
        @DisplayName("중복 평가 질문 체크 시 예외 발생 테스트")
        fun `checking duplicate evaluation question should throw exception`() {
            // Given
            every {
                evaluationQuestionRepository.exists(
                    evaluation = mockEvaluation,
                    question = evaluationQuestionContent,
                )
            } returns true

            // When
            // 같은 Evaluation에 중복된 Question이 있을 경우 Exception
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.checkDuplicateQuestion(
                        evaluation = mockEvaluation,
                        question = evaluationQuestionContent,
                    )
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_QUESTION_CONFLICT, exception.errorCode)
        }

        @Test
        @DisplayName("중복되지 않은 평가 질문 체크 테스트")
        fun `checking non-duplicate evaluation question should not throw exception`() {
            // Given
            every {
                evaluationQuestionRepository.exists(
                    evaluation = mockEvaluation,
                    question = any(),
                )
            } returns false

            // When & Then
            assertDoesNotThrow {
                evaluationService.checkDuplicateQuestion(
                    evaluation = mockEvaluation,
                    question = "NEW_QUESTION",
                )
            }
        }
    }

    @Nested
    @DisplayName("checkDuplicateResultAnswer(): ")
    inner class CheckDuplicateResultAnswer {
        @Test
        @DisplayName("중복 평가 질문 답변 선택 시 예외 발생 테스트")
        fun `should throw BusinessException when selecting duplicate answer for evaluation question`() {
            // Given
            every { // result table에 이미 유저가 question에 대한 답변이 있을 경우
                evaluationResultRepository.exists(
                    reviewId = reviewId,
                    questionId = evaluationQuestionId,
                    userId = userId,
                )
            } returns true

            // When
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.checkDuplicateResultAnswer(
                        review = mockReview,
                        user = mockUser,
                        answer = mockEvaluationAnswer,
                    )
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_RESULT_ANSWER_CONFLICT, exception.errorCode)
        }

        @Test
        @DisplayName("중복되지 않은 평가 질문 답변 체크 테스트")
        fun `should not throw exception when checking non-duplicate answer for evaluation question`() {
            // Given
            every {
                evaluationResultRepository.exists(
                    reviewId = reviewId,
                    questionId = evaluationQuestionId,
                    userId = userId,
                )
            } returns false

            // When & Then
            assertDoesNotThrow {
                evaluationService.checkDuplicateResultAnswer(
                    review = mockReview,
                    user = mockUser,
                    answer = mockEvaluationAnswer,
                )
            }
        }
    }

    @Nested
    @DisplayName("getEvaluationQuestionById(): ")
    inner class GetEvaluationQuestionById {
        @Test
        @DisplayName("EvaluationQuestion ID로 Evaluation Question 엔티티 조회 성공")
        fun `find evaluation question by valid id should return evaluation question`() {
            // Given
            every {
                evaluationQuestionRepository.findById(
                    evaluationQuestionId,
                )
            } returns Optional.of(mockEvaluationQuestion)

            // When
            val result = evaluationService.getEvaluationQuestionById(mockEvaluationQuestion.id!!)

            // Then
            assertEquals(evaluationQuestionId, result.id)
        }

        @Test
        @DisplayName("유효하지 않은 EvaluationQuestion ID로 Evaluation Question 엔티티 조회 시 예외 발생")
        fun `find evaluation question by invalid id should throw exception`() {
            // Given
            val invalidUUID = UUID.randomUUID()
            every {
                evaluationQuestionRepository.findById(invalidUUID)
            } returns Optional.empty()

            // When
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.getEvaluationQuestionById(invalidUUID)
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_QUESTION_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("getEvaluationByType(): ")
    inner class GetEvaluationByType {
        @Test
        @DisplayName("Evaluation Type을 이용하여 Evaluation Entity 조회 성공")
        fun `find evaluation by valid type should return evaluation`() {
            // Given
            every {
                evaluationRepository.find(
                    type = evaluationType,
                )
            } returns mockEvaluation

            // When
            val result = evaluationService.getEvaluationByType(evaluationType)

            // Then
            assertEquals(evaluationId, result.id)
        }

        @Test
        @DisplayName("유효하지 않은 Evaluation Type으로 엔티티 조회 시 예외 발생")
        fun `find evaluation by invalid type should throw exception`() {
            // Given
            val invalidType = EvaluationType.MANAGER
            every {
                evaluationRepository.find(
                    type = invalidType,
                )
            } returns null

            // When
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.getEvaluationByType(invalidType)
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("getEvaluationById(): ")
    inner class GetEvaluationById {
        @Test
        @DisplayName("Evaluation ID를 이용하여 Evaluation 엔티티 조회 시 예외 발생")
        fun `find evaluation by valid id should return evaluation`() {
            // Given
            every {
                evaluationRepository.find(
                    id = evaluationId,
                )
            } returns mockEvaluation

            // When
            val result = evaluationService.getEvaluationById(evaluationId)

            // Then
            assertEquals(evaluationId, result.id)
        }

        @Test
        @DisplayName("유효하지 않은 Evaluation ID로 Evaluation 엔티티 조회 실패")
        fun `find evaluation by invalid id should throw exception`() {
            // Given
            val invalidUUID = UUID.randomUUID()
            every {
                evaluationRepository.find(
                    id = invalidUUID,
                )
            } returns null

            // When
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.getEvaluationById(invalidUUID)
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("getEvaluationAnswerById(): ")
    inner class GetEvaluationAnswerById {
        @Test
        @DisplayName("EvaluationAnswer ID를 이용하여 EvaluationAnswer 엔티티 조회 성공")
        fun `find evaluation answer by valid id should return evaluation answer`() {
            // Given
            every {
                evaluationAnswerRepository.findById(evaluationAnswerId)
            } returns Optional.of(mockEvaluationAnswer)

            // When
            val result =
                assertDoesNotThrow<EvaluationAnswer> {
                    evaluationService.getEvaluationAnswerById(evaluationAnswerId)
                }

            // Then
            assertEquals(evaluationAnswerId, result.id)
        }

        @Test
        @DisplayName("유효하지 않은 EvaluationAnswer ID를 이용하여 엔티티 조회 시 예외 발생")
        fun `find evaluation answer by invalid id should throw exception`() {
            // Given
            val invalidUUID = UUID.randomUUID()
            every {
                evaluationAnswerRepository.findById(invalidUUID)
            } returns Optional.empty()

            // When
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.getEvaluationAnswerById(invalidUUID)
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_ANSWER_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("createEvaluationResult(): ")
    inner class CreateEvaluationResult {
        @Test
        @DisplayName("유효한 평가 결과 등록 성공")
        fun `should create evaluation results with boundary scores 0_5 and 5_0`() {
            // Given
            every { // 유효한 EvaluationAnswer ID로 조회시 같은 UUID를 가진 Entity를 반환하도록 설정
                evaluationAnswerRepository.findById(evaluationAnswerId)
            } returns Optional.of(mockEvaluationAnswer)
            every {
                evaluationResultRepository.exists(
                    reviewId = any(),
                    questionId = any(),
                    userId = any(),
                )
            } returns false

            val evaluationId = UUID.randomUUID()
            val mockEvaluationResult =
                mockk<EvaluationResult> {
                    every { id } returns evaluationId
                    every { review } returns mockReview
                    every { answer } returns mockEvaluationAnswer
                    every { user } returns mockUser
                }

            every {
                evaluationResultRepository.save(
                    match {
                        it.review == mockReview &&
                            it.answer.id == evaluationAnswerId &&
                            it.user == mockUser
                    },
                )
            } returns mockEvaluationResult

            // When
            val result =
                assertDoesNotThrow<EvaluationResult> {
                    evaluationService.createEvaluationResult(
                        user = mockUser,
                        review = mockReview,
                        answerId = evaluationAnswerId,
                    )
                }

            // Then
            assertEquals(evaluationId, result.id)
        }

        @Test
        @DisplayName("유효하지 않은 AnswerID를 이용하여 평가 결과 등록 시 예외 발생")
        fun `should throw exception when creating evaluation result with invalid answer id`() {
            // Given
            val invalidUUID = UUID.randomUUID()
            every { // 유효하지 않은 UUID로 EvaluationAnswer 조회 시 Empty를 반환하도록 설정
                evaluationAnswerRepository.findById(invalidUUID)
            } returns Optional.empty()
            every {
                evaluationResultRepository.exists(
                    reviewId = any(),
                    questionId = any(),
                    userId = any(),
                )
            } returns false

            // When
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.createEvaluationResult(
                        user = mockUser,
                        review = mockReview,
                        answerId = invalidUUID,
                    )
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_ANSWER_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("유저가 한 리뷰에 있는 하나의 질문에 두개의 답변 등록 시 예외 발생 (유효하지 않은 케이스)")
        fun `should throw exception when registering two answers for one question`() {
            // Given
            every {
                evaluationAnswerRepository.findById(evaluationAnswerId)
            } returns Optional.of(mockEvaluationAnswer)
            every {
                evaluationResultRepository.exists(
                    reviewId = any(),
                    questionId = any(),
                    userId = any(),
                )
            } returns true

            // When
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.createEvaluationResult(
                        user = mockUser,
                        review = mockReview,
                        answerId = evaluationAnswerId,
                    )
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_RESULT_ANSWER_CONFLICT, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("createEvaluationAnswer(): ")
    inner class CreateEvaluationAnswer {
        @Test
        @DisplayName("평가 질문에 중복된 답변을 등록 시 예외 발생")
        fun `should throw BusinessException when registering duplicate answer for evaluation question`() {
            val invalidAnswer = "TEST_EVALUATION_ANSWER"

            // Given
            every {
                evaluationQuestionRepository.findById(evaluationQuestionId)
            } returns Optional.of(mockEvaluationQuestion)

            every {
                evaluationAnswerRepository.existsByQuestionAndAnswer(mockEvaluationQuestion, invalidAnswer)
            } returns true

            // When
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.createEvaluationAnswer(
                        questionId = evaluationQuestionId,
                        answer = invalidAnswer,
                    )
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_ANSWER_CONFLICT, exception.errorCode)
        }

        @Test
        @DisplayName("유효하지 않은 Question ID를 이용하여 등록 시 예외 발생")
        fun `should throw BusinessException when creating evaluation answer with invalid question id`() {
            // Given
            val invalidUUID = UUID.randomUUID()
            every {
                evaluationQuestionRepository.findById(invalidUUID)
            } returns Optional.empty()

            // When
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.createEvaluationAnswer(
                        questionId = invalidUUID,
                        answer = "TEST_EVALUATION_ANSWER",
                    )
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_QUESTION_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("유효한 평가 항목 답변 등록 성공")
        fun `should successfully create evaluation answer with valid input`() {
            // Given
            every {
                evaluationQuestionRepository.findById(evaluationQuestionId)
            } returns Optional.of(mockEvaluationQuestion)

            every {
                evaluationAnswerRepository.existsByQuestionAndAnswer(mockEvaluationQuestion, "NEW_EVALUATION_ANSWER")
            } returns false

            every {
                evaluationAnswerRepository.save(any())
            } returns mockEvaluationAnswer

            // When
            val result =
                assertDoesNotThrow<EvaluationAnswer> {
                    evaluationService.createEvaluationAnswer(
                        questionId = evaluationQuestionId,
                        answer = "NEW_EVALUATION_ANSWER",
                    )
                }

            // Then
            assertEquals(evaluationAnswerId, result.id)
        }
    }

    @Nested
    @DisplayName("createEvaluationQuestion(): ")
    inner class CreateEvaluationQuestion {
        @Test
        @DisplayName("유효하지 않은 평가 항목 ID를 이용하여 평가 항목 질문 등록 시 예외 발생")
        fun `should throw exception when creating evaluation question with invalid evaluation id`() {
            // Given
            val invalidUUID = UUID.randomUUID()
            every {
                evaluationRepository.find(
                    id = invalidUUID,
                )
            } returns null

            // When
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.createEvaluationQuestion(
                        evaluationId = invalidUUID,
                        question = "TEST_EVALUATION_QUESTION",
                    )
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("한 평가 항목에 중복된 질문 생성 시 예외 발생")
        fun `should throw exception when creating duplicate question for same evaluation`() {
            // Given
            val duplicateQuestion = "DUP_QUESTION"
            every {
                evaluationRepository.find(
                    id = evaluationId,
                )
            } returns mockEvaluation

            every {
                evaluationQuestionRepository.exists(
                    evaluation = mockEvaluation,
                    question = duplicateQuestion,
                )
            } returns true

            // When
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.createEvaluationQuestion(
                        evaluationId = evaluationId,
                        question = duplicateQuestion,
                    )
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_QUESTION_CONFLICT, exception.errorCode)
        }

        @Test
        @DisplayName("유효한 평가 항목과 질문을 이용하여 평가 항목 질문 등록 생성 테스트 성공")
        fun `should successfully create evaluation question with valid evaluation and question`() {
            // Given
            val evaluationQuestion = "NEW_EVALUATION_QUESTION"
            every {
                evaluationRepository.find(
                    id = evaluationId,
                )
            } returns mockEvaluation

            every {
                evaluationQuestionRepository.exists(
                    evaluation = mockEvaluation,
                    question = any(),
                )
            } returns false

            every {
                evaluationQuestionRepository.save(
                    match {
                        it.evaluation == mockEvaluation &&
                            it.question == evaluationQuestion
                    },
                )
            } returns
                mockk<EvaluationQuestion> {
                    every { id } returns evaluationQuestionId
                    every { evaluation } returns mockEvaluation
                    every { question } returns evaluationQuestion
                }

            // When
            val result =
                assertDoesNotThrow<EvaluationQuestion> {
                    evaluationService.createEvaluationQuestion(
                        evaluationId = evaluationId,
                        question = evaluationQuestion,
                    )
                }

            // Then
            assertEquals(evaluationQuestion, result.question)
        }
    }

    @Nested
    @DisplayName("createEvaluation(): ")
    inner class CreateEvaluation {
        @Test
        @DisplayName("중복된 평가 타입을 이용해 평가 항목을 등록 시 예외 발생")
        fun `should throw exception when registering evaluation with duplicate type`() {
            // Given
            val invalidType = EvaluationType.PEER
            every {
                evaluationRepository.exists(
                    type = invalidType,
                )
            } returns true

            // When
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.createEvaluation(
                        evaluationType = invalidType,
                    )
                }

            // Then
            assertEquals(ErrorCode.EVALUATION_TYPE_CONFLICT, exception.errorCode)
        }

        @Test
        @DisplayName("중복되지 않은(유효한) 평가 항목 등록 테스트 성공")
        fun `should successfully register evaluation with non-duplicate valid type`() {
            // Given
            every {
                evaluationRepository.exists(
                    type = evaluationType,
                )
            } returns false

            every {
                evaluationRepository.save(
                    match {
                        it.type == evaluationType
                    },
                )
            } returns mockEvaluation

            // When
            val result =
                assertDoesNotThrow<Evaluation> {
                    evaluationService.createEvaluation(
                        evaluationType = evaluationType,
                    )
                }

            // Then
            assertEquals(evaluationType, result.type)
        }
    }

    @Nested
    @DisplayName("getEvaluationResultByQuestionId(): ")
    inner class GetEvaluationResultByQuestionId {
        @Test
        @DisplayName("유효한 유저, 리뷰, 질문을 이용하여 평가 답변 조회 테스트 성공")
        fun `getEvaluationResultByQuestionId should return EvaluationResult when found`() {
            // Given
            every {
                evaluationResultRepository.find(
                    userId = userId,
                    reviewId = reviewId,
                    questionId = evaluationQuestionId,
                )
            } returns
                mockk {
                    every { id } returns UUID.randomUUID()
                    every { review } returns mockReview
                    every { answer } returns mockEvaluationAnswer
                    every { user } returns mockUser
                }

            // When & Then
            assertDoesNotThrow<EvaluationResult> {
                evaluationService.getEvaluationResultByQuestionId(
                    userId = userId,
                    reviewId = reviewId,
                    questionId = evaluationQuestionId,
                )
            }
        }

        @Test
        @DisplayName("유효하지 않은 ID를 이용하여 평가 단변 조회 테스트 시 예외 발생")
        fun `getEvaluationResultByQuestionId should throw BusinessException when not found`() {
            // Given
            every {
                evaluationResultRepository.find(
                    userId = any(),
                    reviewId = any(),
                    questionId = any(),
                )
            } returns null

            // When & Then
            val exception =
                assertThrows<BusinessException> {
                    evaluationService.getEvaluationResultByQuestionId(
                        userId = UUID.randomUUID(),
                        reviewId = UUID.randomUUID(),
                        questionId = UUID.randomUUID(),
                    )
                }
            assertEquals(ErrorCode.EVALUATION_RESULT_NOT_FOUND, exception.errorCode)
        }
    }
}
