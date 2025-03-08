package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
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
import kotlin.test.fail

@SpringBootTest
@Import(EvaluationCustomRepositoryImpl::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EvaluationCustomRepositoryImplTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var evaluationRepository: EvaluationRepository

    @PersistenceContext
    private lateinit var entityManager: EntityManager
    private lateinit var jpaQueryFactory: JPAQueryFactory
    private lateinit var evaluationId: UUID

    private val mockEvaluation: Evaluation by lazy {
        Evaluation(
            type = "TEST_EVALUATION",
        ).apply {
            entityManager.persist(this)
            entityManager.flush()
        }
    }

    @BeforeEach
    fun setUp() {
        jpaQueryFactory = JPAQueryFactory(entityManager)
        evaluationId = mockEvaluation.id!!
    }

    @Nested
    @DisplayName("find(): ")
    inner class Find {
        @Test
        @DisplayName("유효한 ID 이용해서 조회")
        fun `find by id`() {
            // When
            val findEntity = evaluationRepository.find(
                id = evaluationId,
            )

            // Then
            findEntity?.let {
                assertEquals(mockEvaluation.type, it.type)
            } ?: run {
                fail()
            }
        }

        @Test
        @DisplayName("유효하지 않은 ID를 이용하여 조회")
        fun `find by invalidId`() {
            // Given
            val invalidUUID = UUID.randomUUID()

            // When
            val findEntity = evaluationRepository.find(
                id = invalidUUID
            )

            // Then
            findEntity?.let {
                fail()
            }
        }

        @Test
        @DisplayName("type 만 이용해서 조회")
        fun `find by type`() {
            // When
            val findEntity = evaluationRepository.find(
                type = mockEvaluation.type
            )

            // Then
            findEntity?.let {
                assertEquals(mockEvaluation.type, it.type)
            } ?: run {
                fail()
            }
        }

        @Test
        @DisplayName("유효하지 않은 Type을 이용하여 조회")
        fun `find by invalid type`() {
            // Given
            val invalidType = "INVALID_TYPE"

            // When
            val findEntity = evaluationRepository.find(
                type = invalidType
            )

            // Then
            findEntity?.let {
                fail()
            }
        }

        @Test
        @DisplayName("id 와 type 을 이용해서 조회")
        fun `find by id and type`() {
            // When
            val findEntity = evaluationRepository.find(
                id = evaluationId,
                type = mockEvaluation.type
            )

            // Then
            findEntity?.let {
                assertEquals(mockEvaluation.id, it.id)
                assertEquals(mockEvaluation.type, it.type)
            } ?: run {
                fail()
            }
        }

        @Test
        @DisplayName("유효하지 않은 ID와 유효한 Type을 이용하여 조회")
        fun `find by invalid id and type`() {
            // When
            val findEntity = evaluationRepository.find(
                id = evaluationId,
                type = mockEvaluation.type
            )

            // Then
            findEntity?.let {
                assertEquals(mockEvaluation.id, it.id)
                assertEquals(mockEvaluation.type, it.type)
            } ?: run {
                fail()
            }
        }

        @Test
        @DisplayName("유효한 ID와 유효하지 않은 Type을 이용하여 조회")
        fun `find by id and invalid type`() {
            // Given
            val invalidUUID = UUID.randomUUID()

            // When
            val findEntity = evaluationRepository.find(
                id = invalidUUID,
                type = mockEvaluation.type
            )

            // Then
            findEntity?.let {
                fail()
            }
        }
    }

    @Nested
    @DisplayName("exists(): ")
    inner class Exists {
        @Test
        @DisplayName("유효한 Type을 이용해 테스트")
        fun `exists by type`() {
            // When
            val result = assertDoesNotThrow {
                evaluationRepository.exists(
                    type = mockEvaluation.type
                )
            }

            // Then
            assertTrue(result)
        }

        @Test
        @DisplayName("유효하지 않은 Type을 이용해 테스트")
        fun `exists by invalid type`() {
            // Given
            val invalidType = "INVALID_TYPE"

            // When
            val result = assertDoesNotThrow {
                evaluationRepository.exists(
                    type = invalidType
                )
            }

            // Then
            assertFalse(result)
        }
    }
}