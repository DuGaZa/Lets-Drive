package com.dugaza.letsdrive.repository

import com.dugaza.letsdrive.entity.common.Review
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ReviewRepository : JpaRepository<Review, UUID> {


    fun findAllByTargetId(targetId: UUID): List<Review>
}
