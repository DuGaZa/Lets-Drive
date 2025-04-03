package com.dugaza.letsdrive.repository.course

import com.dugaza.letsdrive.entity.course.QCourse
import com.dugaza.letsdrive.repository.common.Checks
import com.dugaza.letsdrive.repository.common.Checks.eqIfNotNull
import com.querydsl.jpa.impl.JPAQueryFactory
import java.util.UUID

class CourseCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : CourseCustomRepository {
    override fun exists(courseId: UUID?): Boolean {
        Checks.argsIsNotNull(courseId)

        val course = QCourse.course
        val fetchFirst =
            jpaQueryFactory
                .selectOne()
                .from(course)
                .where(
                    course.id.eqIfNotNull(courseId),
                )
                .fetchFirst()

        return fetchFirst != null
    }
}
