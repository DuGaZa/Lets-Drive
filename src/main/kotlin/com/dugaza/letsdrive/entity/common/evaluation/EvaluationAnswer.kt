package com.dugaza.letsdrive.entity.common.evaluation

import com.dugaza.letsdrive.entity.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete

@Entity
@Table(name = "common_evaluation_answer")
@SQLDelete(sql = "UPDATE common_evaluation_answer SET deleted_at = NOW() WHERE id = ?")
class EvaluationAnswer(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val question: EvaluationQuestion,
    @Column(nullable = false)
    val answer: String,
) : BaseEntity()
