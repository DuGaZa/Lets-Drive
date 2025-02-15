package com.dugaza.letsdrive.repository.file

import com.dugaza.letsdrive.entity.file.FileDetail
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FileDetailRepository : JpaRepository<FileDetail, UUID> {
    fun findByFileMasterId(fileMasterId: UUID): List<FileDetail>

    fun findByFileHash(fileHash: String): FileDetail?
}
