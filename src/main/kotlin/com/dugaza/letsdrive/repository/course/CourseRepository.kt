package com.dugaza.letsdrive.repository.course

import com.dugaza.letsdrive.entity.course.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CourseRepository : JpaRepository<Course, UUID>
