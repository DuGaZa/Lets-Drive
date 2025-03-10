package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationAnswer
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
@Import(EvaluationAnswerCustomRepositoryImpl::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EvaluationAnswerCustomRepositoryImplTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var evaluationAnswerRepository: EvaluationAnswerRepository

    @PersistenceContext
    private lateinit var entityManager: EntityManager
    private lateinit var jpaQueryFactory: JPAQueryFactory

    private val mockEvaluation: Evaluation by lazy {
        Evaluation(
            type = "TEST_EVALUATION",
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }
    private val mockEvaluationQuestion1: EvaluationQuestion by lazy {
        EvaluationQuestion(
            evaluation = mockEvaluation,
            question = "TEST_QUESTION_1",
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }
    private val mockEvaluationQuestion2: EvaluationQuestion by lazy {
        EvaluationQuestion(
            evaluation = mockEvaluation,
            question = "TEST_QUESTION_2",
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }
    private val mockEvaluationQuestion1Answer1: EvaluationAnswer by lazy {
        EvaluationAnswer(
            question = mockEvaluationQuestion1,
            answer = "TEST_QUESTION_1_ANSWER_1",
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }
    private val mockEvaluationQuestion1Answer2: EvaluationAnswer by lazy {
        EvaluationAnswer(
            question = mockEvaluationQuestion1,
            answer = "TEST_QUESTION_1_ANSWER_2",
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }
    private val mockEvaluationQuestion2Answer1: EvaluationAnswer by lazy {
        EvaluationAnswer(
            question = mockEvaluationQuestion2,
            answer = "TEST_QUESTION_2_ANSWER_1",
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }
    private val mockEvaluationQuestion2Answer2: EvaluationAnswer by lazy {
        EvaluationAnswer(
            question = mockEvaluationQuestion2,
            answer = "TEST_QUESTION_2_ANSWER_2",
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
        @DisplayName("유효한 Question과 Answer를 이용해 테스트")
        fun `exists by question and answer`() {
            // When
            val result = assertDoesNotThrow {
                evaluationAnswerRepository.exists(
                    question = mockEvaluationQuestion1,
                    answer = mockEvaluationQuestion1Answer1.answer
                )
            }

            // Then
            assertTrue(result)
        }

        @Test
        @DisplayName("유효한 Question과 유효하지 않은 Answer를 이용해 테스트")
        fun `exists valid question and invalid answer`() {
            // When
            val result = assertDoesNotThrow {
                evaluationAnswerRepository.exists(
                    question = mockEvaluationQuestion1,
                    answer = "INVALID_ANSWER"
                )
            }

            // Then
            assertFalse(result)
        }
    }
}