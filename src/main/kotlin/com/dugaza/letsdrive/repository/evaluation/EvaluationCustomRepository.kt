package com.dugaza.letsdrive.repository.evaluation

import com.dugaza.letsdrive.entity.common.evaluation.Evaluation

interface EvaluationCustomRepository {
    fun find(type: String): Evaluation?

    fun exists(type: String): Boolean
}
