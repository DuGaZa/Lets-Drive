package com.dugaza.letsdrive.repository.course

import java.util.UUID

interface CourseCustomRepository {
    fun exists(courseId: UUID?): Boolean
}
