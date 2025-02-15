package com.dugaza.letsdrive.entity.common.evaluation

import com.dugaza.letsdrive.entity.base.BaseEntity
import com.dugaza.letsdrive.entity.common.Review
import com.dugaza.letsdrive.entity.user.User
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "common_evaluation_result")
class EvaluationResult(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    val review: Review,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    var answer: EvaluationAnswer,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
) : BaseEntity()
