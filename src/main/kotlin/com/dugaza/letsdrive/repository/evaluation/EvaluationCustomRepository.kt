package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import java.util.UUID

interface EvaluationCustomRepository {
    fun find(type: String): Evaluation?
    fun find(id: UUID): Evaluation?

    fun exists(type: String): Boolean
}
