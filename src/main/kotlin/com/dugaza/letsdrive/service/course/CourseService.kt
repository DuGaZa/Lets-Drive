package com.dugaza.letsdrive.service.course

import com.dugaza.letsdrive.entity.course.Course
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.repository.course.CourseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class CourseService(
    private val courseRepository: CourseRepository,
) {
    /**
     * Course UUID를 이용하여 엔티티 조회
     * @param courseId COURSE UUID
     * @return 조회된 COURSE Entity
     * @exception BusinessException ErrorCode.COURSE_NOT_FOUND
     */
    fun getCourseById(courseId: UUID): Course {
        return courseRepository.findById(courseId)
            .orElseThrow {
                BusinessException(ErrorCode.COURSE_NOT_FOUND)
            }
    }

    /**
     * Course 존재 여부 확인 (ID)
     *
     * @param courseId Course ID
     * @exception BusinessException ErrorCode.COURSE_NOT_FOUND
     */
    fun existsCourseById(courseId: UUID) {
        courseRepository.exists(
            courseId = courseId,
        ).takeIf { it } ?: throw BusinessException(ErrorCode.COURSE_NOT_FOUND)
    }
}
