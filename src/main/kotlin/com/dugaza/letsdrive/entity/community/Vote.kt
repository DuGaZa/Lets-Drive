package com.dugaza.letsdrive.entity.community

import com.dugaza.letsdrive.entity.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete

@Entity
@Table(name = "community_vote")
@SQLDelete(sql = "UPDATE community_vote SET deleted_at = NOW() WHERE id = ?")
class Vote(
    @Column(nullable = false, length = 255)
    var title: String,
) : BaseEntity()
