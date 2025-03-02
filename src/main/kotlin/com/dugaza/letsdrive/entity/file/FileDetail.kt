package com.dugaza.letsdrive.entity.file

import com.dugaza.letsdrive.entity.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete

@Entity
@Table(
    name = "file_detail",
    indexes = [
        Index(name = "idx_file_detail_file_master_id", columnList = "file_master_id"),
        Index(name = "idx_file_detail_file_hash", columnList = "file_hash"),
    ],
)
@SQLDelete(sql = "UPDATE file_detail SET deleted_at = NOW() WHERE id = ?")
class FileDetail(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_master_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val fileMaster: FileMaster,
    @Column(nullable = false)
    val originalName: String,
    @Column(nullable = false)
    val storedName: String,
    @Column(nullable = false)
    val storedPath: String,
    @Column(nullable = false)
    val originalSize: Long,
    @Column(nullable = false)
    val storedSize: Long,
    @Column(nullable = false)
    val originalExtension: String,
    @Column(nullable = false)
    val storedExtension: String,
    @Column(nullable = false, length = 255)
    val mimeType: String,
    @Column(nullable = false, length = 64, unique = true)
    val fileHash: String,
    @Column(nullable = true)
    var thumbnailPath: String? = null,
    @Column(nullable = false)
    var compressed: Boolean = false,
) : BaseEntity()
