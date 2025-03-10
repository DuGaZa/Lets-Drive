package com.dugaza.letsdrive.repository.review

import com.dugaza.letsdrive.entity.common.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ReviewCustomRepository {
    fun findAllWithPage(
        targetId: UUID?,
        pageable: Pageable,
    ): Page<Review>
}