package com.dugaza.letsdrive.entity.common.evaluation

import com.dugaza.letsdrive.entity.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete

@Entity
@Table(name = "common_evaluation")
@SQLDelete(sql = "UPDATE common_evaluation SET deleted_at = NOW() WHERE id = ?")
class Evaluation(
    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type", nullable = false)
    val type: EvaluationType,
) : BaseEntity()
