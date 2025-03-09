package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import com.dugaza.letsdrive.integration.BaseIntegrationTest
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
@Import(EvaluationQuestionCustomRepositoryImpl::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EvaluationQuestionCustomRepositoryImplTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var evaluationQuestionRepository: EvaluationQuestionRepository

    @PersistenceContext
    private lateinit var entityManager: EntityManager
    private lateinit var jpaQueryFactory: JPAQueryFactory
    private lateinit var evaluation1Id: UUID
    private lateinit var evaluation2Id: UUID
    private lateinit var evaluation1Question1Id: UUID
    private lateinit var evaluation1Question2Id: UUID
    private lateinit var evaluation2Question1Id: UUID
    private lateinit var evaluation2Question2Id: UUID

    private val mockEvaluation1: Evaluation by lazy {
        Evaluation(
            type = "TEST_EVALUATION_1"
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }
    private val mockEvaluation2: Evaluation by lazy {
        Evaluation(
            type = "TEST_EVALUATION_2"
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }

    private val mockEvaluation1Question1: EvaluationQuestion by lazy {
        EvaluationQuestion(
            evaluation = mockEvaluation1,
            question = "TEST_EVALUATION_1_QUESTION_1",
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }
    private val mockEvaluation1Question2: EvaluationQuestion by lazy {
        EvaluationQuestion(
            evaluation = mockEvaluation1,
            question = "TEST_EVALUATION_1_QUESTION_2",
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }
    private val mockEvaluation2Question1: EvaluationQuestion by lazy {
        EvaluationQuestion(
            evaluation = mockEvaluation2,
            question = "TEST_EVALUATION_2_QUESTION_1",
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }
    private val mockEvaluation2Question2: EvaluationQuestion by lazy {
        EvaluationQuestion(
            evaluation = mockEvaluation2,
            question = "TEST_EVALUATION_2_QUESTION_2",
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }

    @BeforeEach
    fun setUp() {
        jpaQueryFactory = JPAQueryFactory(entityManager)
        evaluation1Id = mockEvaluation1.id!!
        evaluation2Id = mockEvaluation2.id!!
        evaluation1Question1Id = mockEvaluation1Question1.id!!
        evaluation1Question2Id = mockEvaluation1Question2.id!!
        evaluation2Question1Id = mockEvaluation2Question1.id!!
        evaluation2Question2Id = mockEvaluation2Question2.id!!
    }

    @Nested
    @DisplayName("exists(): ")
    inner class Exists {
        @Test
        @DisplayName("유효한 Evaluation과 Question을 이용해 테스트")
        fun `exists by valid evaluation and valid question`() {
            // When
            val result = assertDoesNotThrow<Boolean> {
                evaluationQuestionRepository.exists(
                    evaluation = mockEvaluation1,
                    question = mockEvaluation1Question1.question,
                )
            }

            // Then
            assertTrue(result)
        }

        @Test
        @DisplayName("유효한 Evaluation과 유효하지 않은 Question을 이용해 테스트")
        fun `exists by valid evaluation and invalid question`() {
            // Given
            val invalidQuestion = "INVALID_QUESTION"

            // When
            val result = assertDoesNotThrow<Boolean> {
                evaluationQuestionRepository.exists(
                    evaluation = mockEvaluation1,
                    question = invalidQuestion,
                )
            }

            // Then
            assertFalse(result)
        }
    }

    @Nested
    @DisplayName("findAll(): ")
    inner class FindAll {
        @Test
        @DisplayName("유효한 Evaluation ID를 이용하여 조회")
        fun `findAll by valid evaluationId`() {
            // When
            // evaluation1에 대한 Question
            val evaluationQuestionList1 = assertDoesNotThrow {
                evaluationQuestionRepository.findAll(
                    evaluationId = evaluation1Id,
                )
            }
            // evaluation2에 대한 Question
            val evaluationQuestionList2 = assertDoesNotThrow {
                evaluationQuestionRepository.findAll(
                    evaluationId = evaluation2Id,
                )
            }

            // Then
            assertTrue {
                evaluationQuestionList1.all {
                    mockEvaluation1.id == it.evaluation.id
                }
            }
            assertTrue {
                evaluationQuestionList2.all {
                    mockEvaluation2.id == it.evaluation.id
                }
            }
        }

        @Test
        @DisplayName("유효하지 않은 Evaluation ID를 이용해 조회")
        fun `findAll by invalid evaluationId`() {
            // Given
            val invalidUUID = UUID.randomUUID()

            // When
            val result = assertDoesNotThrow {
                evaluationQuestionRepository.findAll(
                    evaluationId = invalidUUID,
                )
            }

            // Then
            assertEquals(0, result.size)
        }
    }
}