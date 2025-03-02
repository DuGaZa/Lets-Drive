package com.dugaza.letsdrive.entity.common

import com.dugaza.letsdrive.entity.base.BaseEntity
import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.file.FileMaster
import com.dugaza.letsdrive.entity.user.User
import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import java.util.UUID

@Entity
@Table(
    name = "common_review",
    indexes = [
        Index(name = "idx_common_review_target_id", columnList = "target_id"),
    ],
)
@SQLDelete(sql = "UPDATE common_review SET deleted_at = NOW() WHERE id = ?")
class Review(
    @Column(nullable = false)
    val targetId: UUID,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val evaluation: Evaluation,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = true, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    var file: FileMaster,
    @Column(nullable = false)
    var score: Double = 0.0,
    @Column(nullable = false)
    var content: String,
    @Column(nullable = false)
    var isDisplayed: Boolean = false,
) : BaseEntity()
