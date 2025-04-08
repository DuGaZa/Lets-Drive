package com.dugaza.letsdrive.entity.common.evaluation

import com.dugaza.letsdrive.entity.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "common_evaluation")
class Evaluation(
    @Column(name = "evaluation_type", nullable = false)
    val type: EvaluationType,
) : BaseEntity()
