package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.Optional

@Repository
interface EvaluationRepository : JpaRepository<Evaluation, UUID> {
    fun findByType(type: String): Optional<Evaluation>
    fun existsByType(type: String): Boolean
}