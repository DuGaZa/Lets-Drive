package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import java.util.UUID

interface EvaluationCustomRepository {
    fun find(id: UUID? = null, type: String? = null): Evaluation?

    fun exists(type: String): Boolean
}
