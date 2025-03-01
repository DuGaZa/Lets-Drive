package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EvaluationQuestionRepository : JpaRepository<EvaluationQuestion, UUID>, EvaluationQuestionCustomRepository
