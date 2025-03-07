package com.dugaza.letsdrive.repository.course

import com.dugaza.letsdrive.entity.course.Course
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CourseRepository : JpaRepository<Course, UUID>
