package com.dugaza.letsdrive.entity.course

import com.dugaza.letsdrive.entity.base.BaseEntity
import com.dugaza.letsdrive.entity.user.User
import jakarta.persistence.*

@Entity
@Table(name = "Course")
class Course(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Column(name = "course_name", nullable = false)
    val name: String,
) : BaseEntity() {
}