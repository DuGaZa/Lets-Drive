package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationType
import java.util.UUID

interface EvaluationCustomRepository {
    fun find(
        id: UUID? = null,
        type: EvaluationType? = null,
    ): Evaluation?

    fun exists(type: EvaluationType): Boolean
}
