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
import java.util.UUID

@Entity
@Table(
    name = "common_review",
    indexes = [
        Index(name = "idx_common_review_target_id", columnList = "target_id"),
    ],
)
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
) : BaseEntity() {
    /**
     * Review 객체의 점수와 내용을 선택적으로 업데이트합니다.
     *
     * @param score 업데이트할 리뷰 점수.
     *              값이 제공되면 해당 값으로 업데이트되고,
     *              생략되면 기존 점수를 유지합니다.
     * @param content 업데이트할 리뷰 내용.
     *                값이 제공되면 해당 값으로 업데이트되고,
     *                생략되면 기존 내용을 유지합니다.
     *
     * 이 함수는 제공된 매개변수에 따라 부분적인 업데이트를 수행합니다.
     * 두 매개변수 모두 생략하면 아무 변경도 일어나지 않습니다.
     *
     * 사용 예:
     * - review.update(score = 4.5) // 점수만 업데이트
     * - review.update(content = "새로운 내용") // 내용만 업데이트
     * - review.update(score = 4.5, content = "새로운 내용") // 둘 다 업데이트
     * - review.update() // 아무 변경 없음
     */
    fun update(
        score: Double? = null,
        content: String? = null,
    ) {
        score?.let { this.score = it }
        content?.let { this.content = it }
    }
}
