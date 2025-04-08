package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.EvaluationResult
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EvaluationResultRepository : JpaRepository<EvaluationResult, UUID>, EvaluationResultCustomRepository
