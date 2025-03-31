package com.dugaza.letsdrive.repository.course

import com.dugaza.letsdrive.entity.course.Course
import com.dugaza.letsdrive.entity.user.AuthProvider
import com.dugaza.letsdrive.entity.user.User
import com.dugaza.letsdrive.entity.user.UserStatus
import com.dugaza.letsdrive.integration.BaseIntegrationTest
import com.querydsl.jpa.impl.JPAQueryFactory
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.Assert.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.time.LocalDateTime
import java.util.UUID.randomUUID
import kotlin.test.Test
import kotlin.test.assertTrue

@SpringBootTest
@Import(CourseCustomRepositoryImpl::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CourseCustomRepositoryImplTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var courseRepository: CourseRepository

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

    private val mockCourse: Course by lazy {
        Course(
            user = mockUser,
            name = "TEST_COURSE",
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
        @DisplayName("유효한 courseId를 이용하여 테스트")
        fun `exists by valid courseId`() {
            // When & Then
             assertDoesNotThrow {
                courseRepository.exists(
                    courseId = mockCourse.id!!,
                )
            }
        }

        @Test
        @DisplayName("유효하지 않은 courseId를 이용하여 테스트")
        fun `exists by invalid courseId`() {
            // Given
            val invalidCourseId = randomUUID()

            // When & Then
            assertDoesNotThrow {
                courseRepository.exists(
                    courseId = invalidCourseId
                )
            }
        }
    }
}