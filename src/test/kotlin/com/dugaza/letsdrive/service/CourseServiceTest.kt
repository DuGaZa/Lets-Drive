package com.dugaza.letsdrive.service

import com.dugaza.letsdrive.entity.course.Course
import com.dugaza.letsdrive.entity.user.AuthProvider
import com.dugaza.letsdrive.entity.user.User
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.repository.course.CourseRepository
import com.dugaza.letsdrive.service.course.CourseService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import java.util.UUID
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class CourseServiceTest {
    @MockK
    private lateinit var courseRepository: CourseRepository

    @InjectMockKs
    private lateinit var courseService: CourseService

    private lateinit var mockCourse1: Course
    private lateinit var mockUser1: User
    private lateinit var courseId: UUID
    private lateinit var userId: UUID

    @BeforeEach
    fun setUp() {
        courseId = UUID.randomUUID()
        userId = UUID.randomUUID()

        mockUser1 =
            mockk<User> {
                every { id } returns userId
                every { email } returns "mock@example.com"
                every { provider } returns AuthProvider.GOOGLE
                every { providerId } returns UUID.randomUUID().toString()
                every { nickname } returns "mockNickname"
            }

        mockCourse1 =
            mockk<Course> {
                every { id } returns courseId
                every { user } returns mockUser1
                every { name } returns "mockCourseName"
            }

        // Repository 동작 설정
        every { courseRepository.findById(courseId) } returns Optional.of(mockCourse1)
    }

    @Test
    @DisplayName("Course ID로 엔티티 조회 성공")
    fun `find course by valid id should return course`() {
        // When
        val result = courseService.getCourseById(mockCourse1.id!!)

        // Then
        assertEquals(courseId, result.id)
    }

    @Test
    @DisplayName("유효하지 않은 Course ID로 엔티티 조회 시 예외 발생")
    fun `find course by invalid id should throw exception`() {
        // Given
        val invalidUUID = UUID.randomUUID()
        every { courseRepository.findById(invalidUUID) } returns Optional.empty()

        // When & Then
        assertThrows<BusinessException> {
            courseService.getCourseById(invalidUUID)
        }
    }
}
