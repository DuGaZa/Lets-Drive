package com.dugaza.letsdrive.entity.common

import com.dugaza.letsdrive.entity.base.BaseEntity
import com.dugaza.letsdrive.entity.user.User
import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID
import org.hibernate.annotations.SQLDelete

@Entity
@Table(
    name = "common_report",
    indexes = [
        Index(name = "idx_common_review_target_id", columnList = "target_id"),
    ],
)
@SQLDelete(sql = "UPDATE common_report SET deleted_at = NOW() WHERE id = ?")
class Report(
    @Column(nullable = false)
    val targetId: UUID,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val user: User,
    @Enumerated(EnumType.STRING)
    @Column(name = "report_reason_code", nullable = false)
    val reasonCode: ReasonCode,
    @Column(name = "report_reason", nullable = false)
    val reason: String,
) : BaseEntity()
