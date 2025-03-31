package com.dugaza.letsdrive.repository.review

import com.dugaza.letsdrive.dto.review.ReviewResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import java.util.UUID

interface ReviewCustomRepository {
    fun findAllByTargetIdWithPage(
        targetId: UUID,
        pageable: Pageable,
    ): PagedModel<ReviewResponse>
}
