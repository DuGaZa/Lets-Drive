package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.Review
import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationAnswer
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationResult
import com.dugaza.letsdrive.entity.course.Course
import com.dugaza.letsdrive.entity.file.FileMaster
import com.dugaza.letsdrive.entity.user.AuthProvider
import com.dugaza.letsdrive.entity.user.User
import com.dugaza.letsdrive.entity.user.UserStatus
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.integration.BaseIntegrationTest
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.time.LocalDateTime
import java.util.UUID.randomUUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

@SpringBootTest
@Import(EvaluationResultCustomRepositoryImpl::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EvaluationResultCustomRepositoryImplTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var evaluationResultRepository: EvaluationResultRepository

    @Autowired
    private lateinit var evaluationAnswerRepository: EvaluationAnswerRepository

    @PersistenceContext
    private lateinit var entityManager: EntityManager
    private lateinit var jpaQueryFactory: JPAQueryFactory

    private val mockUser: User by lazy {
        User(
            email = "mock_user@test.email",
            provider = AuthProvider.GOOGLE,
            providerId = randomUUID().toString(),
            nickname = "MOCK_TEST_USER_NICKNAME",
            status = UserStatus.ACTIVE,
            roles = mutableSetOf(),
            lastLoginAt = LocalDateTime.now(),
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }

    private val mockEvaluation: Evaluation by lazy {
        Evaluation(
            type = "COURSE_EVALUATION"
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }

    private val mockFileMaster: FileMaster by lazy {
        FileMaster(
            user = mockUser,
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }

    private val mockCourse: Course by lazy {
        Course(
            user = mockUser,
            name = "TEST_COURSE",
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }

    private val mockReview: Review by lazy {
        Review(
            targetId = mockCourse.id!!,
            user = mockUser,
            evaluation = mockEvaluation,
            file = mockFileMaster,
            score = 3.5,
            content = "TEST_REVIEW_CONTENT",
            isDisplayed = true,
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }

    private val mockEvaluationQuestion: EvaluationQuestion by lazy {
        EvaluationQuestion(
            evaluation = mockEvaluation,
            question = "TEST_EVALUATION_QUESTION"
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }

    @BeforeEach
    fun setUp() {
        jpaQueryFactory = JPAQueryFactory(entityManager)
    }

    @Nested
    @DisplayName("exists(): ")
    inner class Exists {
        @Test
        @DisplayName("유효한 reviewId, 유효한 questionId, 유효한 userId를 이용하여 테스트")
        fun `exists by valid reviewId and valid questionId and valid userId`() {
            // Given
            val evaluationAnswer = evaluationAnswerRepository.save(
                EvaluationAnswer(
                    question = mockEvaluationQuestion,
                    answer = "TEST_EVALUATION_ANSWER"
                )
            )

            evaluationResultRepository.save(
                EvaluationResult(
                    review = mockReview,
                    answer = evaluationAnswer,
                    user = mockUser,
                )
            )

            // When
            val exists = evaluationResultRepository.exists(
                reviewId = mockReview.id!!,
                questionId = mockEvaluationQuestion.id!!,
                userId = mockUser.id!!,
            )

            // Then
            assertTrue(exists)
        }

        @Test
        @DisplayName("유효하지 않은 reviewId, questionId, userId를 이용하여 테스트")
        fun `exists by invalid reviewId and questionId and userId`() {
            // Given
            val invalidReviewId = randomUUID()
            val invalidQuestionId = randomUUID()
            val invalidUserId = randomUUID()

            // When
            val exists = evaluationResultRepository.exists(
                reviewId = invalidReviewId,
                questionId = invalidQuestionId,
                userId = invalidUserId,
            )

            // Then
            assertFalse(exists)
        }
    }

    @Nested
    @DisplayName("findAll(): ")
    inner class FindAll {
        @Test
        @DisplayName("유효한 userId, reviewId를 이용하여 전체 조회 테스트")
        fun `findAll by valid reviewId and valid reviewId`() {
            // When
            val evaluationResultList = assertDoesNotThrow<List<EvaluationResult>> {
                evaluationResultRepository.findAll(
                    userId = mockUser.id!!,
                    reviewId = mockReview.id!!,
                )
            }

            // Then
            assertTrue {
                evaluationResultList.all {
                    mockUser.id!! == it.user.id!!
                }
            }
            assertTrue {
                evaluationResultList.all {
                    mockReview.id!! == it.review.id!!
                }
            }
        }

        @Test
        @DisplayName("유효하지 않은 userId, reviewId를 이용하여 전체 조회 테스트")
        fun `findAll by invalid reviewId and invalid reviewId`() {
            // Given
            val invalidUserId = randomUUID()
            val invalidReviewId = randomUUID()

            // When
            val result = assertDoesNotThrow {
                evaluationResultRepository.findAll(
                    userId = invalidUserId,
                    reviewId = invalidReviewId,
                )
            }

            // Then
            assertEquals(0, result.size)
        }
    }

    @Nested
    @DisplayName("find(): ")
    inner class Find {
        @Test
        @DisplayName("유효한 userId, reviewId, questionId를 이용해 조회 테스트")
        fun `find by valid userId and reviewId and questionId`() {
            // Given
            val evaluationAnswer = evaluationAnswerRepository.save(
                EvaluationAnswer(
                    question = mockEvaluationQuestion,
                    answer = "TEST_EVALUATION_ANSWER"
                )
            )

            evaluationResultRepository.save(
                EvaluationResult(
                    review = mockReview,
                    answer = evaluationAnswer,
                    user = mockUser,
                )
            )

            // When
            val findEvaluationResult = assertDoesNotThrow {
                evaluationResultRepository.find(
                    userId = mockUser.id!!,
                    reviewId = mockReview.id!!,
                    questionId = mockEvaluationQuestion.id!!,
                )
            }

            // Then
            findEvaluationResult?.let {
                assertEquals(mockUser.id!!, it.user.id!!)
                assertEquals(mockReview.id!!, it.review.id!!)
                assertEquals(mockEvaluationQuestion.id!!, it.answer.question.id!!)
            } ?: run {
                fail()
            }
        }

        @Test
        @DisplayName("유효하지 않은 userId, reviewId, questionId를 이용해 조회 테스트")
        fun `find by invalid userId and reviewId and questionId`() {
            // Given
            val invalidUserId = randomUUID()
            val invalidReviewId = randomUUID()
            val invalidQuestionId = randomUUID()

            // When
            val findEvaluationResult = assertDoesNotThrow {
                evaluationResultRepository.find(
                    userId = invalidUserId,
                    reviewId = invalidReviewId,
                    questionId = invalidQuestionId,
                )
            }

            // Then
            findEvaluationResult?.let {
                fail()
            }
        }
    }

    @Nested
    @DisplayName("delete(): ")
    inner class Delete {
        @Test
        @DisplayName("유효한 review Id를 이용해 Evaluation Result 삭제 테스트")
        fun `delete by valid reviewId`() {
            // Given
            val evaluationAnswer = evaluationAnswerRepository.save(
                EvaluationAnswer(
                    question = mockEvaluationQuestion,
                    answer = "TEST_EVALUATION_ANSWER"
                )
            )

            evaluationResultRepository.save(
                EvaluationResult(
                    review = mockReview,
                    answer = evaluationAnswer,
                    user = mockUser,
                )
            )

            // When & Then
            assertDoesNotThrow {
                evaluationResultRepository.delete(
                    reviewId = mockReview.id!!,
                )
            }
        }

        @Test
        @DisplayName("유효하지 않은 review id를 이용해 Evaluation Result 삭제 예외 테스트")
        fun `delete by invalid reviewId`() {
            // Given
            val invalidReviewId = randomUUID()

            // When
            val exception = assertThrows<BusinessException> {
                evaluationResultRepository.delete(
                    reviewId = invalidReviewId,
                )
            }

            // Then
            assertEquals(ErrorCode.EVALUATION_RESULT_NOT_FOUND, exception.errorCode)
        }
    }
}