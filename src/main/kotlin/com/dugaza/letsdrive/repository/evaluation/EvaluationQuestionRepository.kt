package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EvaluationQuestionRepository : JpaRepository<EvaluationQuestion, UUID>, EvaluationQuestionCustomRepository
