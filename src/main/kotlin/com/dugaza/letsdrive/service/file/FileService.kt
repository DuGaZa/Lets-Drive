package com.dugaza.letsdrive.service.file

import com.dugaza.letsdrive.config.FileProperties
import com.dugaza.letsdrive.entity.file.FileDetail
import com.dugaza.letsdrive.entity.file.FileMaster
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.repository.file.FileDetailRepository
import com.dugaza.letsdrive.repository.file.FileMasterRepository
import com.dugaza.letsdrive.service.user.UserService
import net.coobird.thumbnailator.Thumbnails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO
import kotlin.io.path.createDirectories
import kotlin.io.path.writeBytes

@Service
class FileService(
    private val fileDetailRepository: FileDetailRepository,
    private val fileMasterRepository: FileMasterRepository,
    private val userService: UserService,
) {
    private val fileProperties: FileProperties = FileProperties()
    private val maxSize = fileProperties.maxSize
    private val imageExt = fileProperties.imageExtensionSet()
    private val uncompressedExt = fileProperties.uncompressedExtensionSet()
    private val allowedExt = imageExt + fileProperties.allowedExtensionSet()
    private val uploadRoot: Path = Path.of(fileProperties.uploadRoot).createDirectories()
    private val defaultImageDetailId = fileProperties.defaultImageDetailId

    /**
     * 여러 파일 업로드
     * @param userId 사용자 UUID
     * @param files 업로드할 MultipartFile 리스트
     * @return FileMaster와 저장된 FileDetail 리스트의 Pair
     */
    @Transactional
    fun uploadFile(
        userId: UUID,
        files: List<MultipartFile>,
    ): Pair<FileMaster, List<FileDetail>> {
        val user = userService.getUserById(userId)
        val master = fileMasterRepository.save(FileMaster(user))
        val detailList = files.map { saveFileDetail(master, it) }
        val saved = fileDetailRepository.saveAll(detailList)
        return master to saved.toList()
    }

    /**
     * FileMaster 조회
     * @param fileMasterId 파일 마스터 UUID
     * @return FileMaster Entity
     * @exception BusinessException ErrorCode.NOT_FOUND_FILE_MASTER
     */
    fun getFileMaster(fileMasterId: UUID): FileMaster {
        return fileMasterRepository.findById(fileMasterId)
            .orElseThrow { BusinessException(ErrorCode.NOT_FOUND_FILE_MASTER) }
    }

    /**
     * 파일 상세 조회
     * @param detailId 파일 상세 ID
     * @return 파일 상세 엔티티
     */
    fun getFileDetail(detailId: UUID): FileDetail {
        return fileDetailRepository.findById(detailId)
            .orElseThrow { BusinessException(ErrorCode.NOT_FOUND_FILE_DETAIL) }
    }

    /**
     * 파일 다운로드
     * @param detailId 파일 상세 ID
     * @return 파일 바이트
     */
    fun downloadFile(detailId: UUID): ByteArray {
        val detail = getFileDetail(detailId)
        var fileBytes = loadFileContent(detail)

        if (detail.compressed) {
            try {
                fileBytes = unzipSingleFile(fileBytes)
            } catch (e: Exception) {
                throw BusinessException(ErrorCode.FILE_DECOMPRESSION_FAILED)
            }
        }
        return fileBytes
    }

    /**
     * 기본 프로필 이미지 파일 생성
     * @param userId 사용자 UUID
     * @return 생성된 FileMaster 엔티티
     */
    @Transactional
    fun getDefaultImage(userId: UUID): FileMaster {
        val user = userService.getUserById(userId)
        val existingDetail =
            fileDetailRepository.findById(defaultImageDetailId)
                .orElseThrow { BusinessException(ErrorCode.NOT_FOUND_FILE_DETAIL) }
        val fileMaster = fileMasterRepository.save(FileMaster(user))

        val detail = createDuplicateFileDetail(fileMaster, existingDetail, existingDetail.originalName)
        fileDetailRepository.save(detail)

        return fileMaster
    }

    /**
     * 단일 파일 저장 처리
     * @param fileMaster 파일 마스터 엔티티
     * @param file 업로드된 파일
     * @return 저장된 FileDetail 엔티티
     */
    private fun saveFileDetail(
        fileMaster: FileMaster,
        file: MultipartFile,
    ): FileDetail {
        val originalName = file.originalFilename ?: "unknown"
        val mimeType = file.contentType ?: "application/octet-stream"
        val fileBytes = file.bytes
        val originalSize = fileBytes.size.toLong()

        val originalExtension = extractExtension(originalName).lowercase()
        validateExtension(originalExtension)

        val fileHash = computeFileHash(fileBytes)

        val existing = fileDetailRepository.findByFileHash(fileHash)

        if (existing != null) {
            return createDuplicateFileDetail(fileMaster, existing, originalName)
        }

        var thumbnailPath: String? = null
        if (isImage(originalExtension)) {
            thumbnailPath = generateThumbnail(fileBytes, originalExtension)
        }

        val (finalBytes, compressed, storedExtension, storedSize) =
            processCompression(
                fileBytes,
                originalName,
                originalExtension,
                originalSize,
            )

        val storedName = "${UUID.randomUUID()}.$storedExtension"
        val storedPath = uploadRoot.resolve(storedName)
        storedPath.writeBytes(finalBytes)

        return FileDetail(
            fileMaster = fileMaster,
            originalName = originalName,
            storedName = storedName,
            storedPath = storedPath.toString(),
            originalSize = originalSize,
            storedSize = storedSize,
            originalExtension = originalExtension,
            storedExtension = storedExtension,
            mimeType = mimeType,
            fileHash = fileHash,
            thumbnailPath = thumbnailPath,
            compressed = compressed,
        )
    }

    /**
     * 파일 내용 로드
     * @param detail 파일 상세 엔티티
     * @return 파일 바이트
     */
    private fun loadFileContent(detail: FileDetail): ByteArray {
        val path = Path.of(detail.storedPath)
        return Files.readAllBytes(path)
    }

    /**
     * 파일 확장자 유효성 검사
     * @param extension 파일 확장자
     */
    private fun validateExtension(extension: String) {
        if (!allowedExt.contains(extension)) {
            throw BusinessException(ErrorCode.INVALID_EXTENSION)
        }
    }

    /**
     * 이미지 파일 여부 확인
     * @param extension 파일 확장자
     * @return 이미지 파일인지 여부
     */
    private fun isImage(extension: String): Boolean {
        return imageExt.contains(extension)
    }

    /**
     * 중복 파일인 경우 새로운 FileDetail 생성
     * @param fileMaster 파일 마스터 엔티티
     * @param existing 기존 FileDetail 엔티티
     * @param originalName 원본 파일명
     * @return 새로운 FileDetail 엔티티
     */
    private fun createDuplicateFileDetail(
        fileMaster: FileMaster,
        existing: FileDetail,
        originalName: String,
    ): FileDetail {
        return FileDetail(
            fileMaster = fileMaster,
            originalName = originalName,
            storedName = existing.storedName,
            storedPath = existing.storedPath,
            originalSize = existing.originalSize,
            storedSize = existing.storedSize,
            originalExtension = existing.originalExtension,
            storedExtension = existing.storedExtension,
            mimeType = existing.mimeType,
            fileHash = existing.fileHash,
            thumbnailPath = existing.thumbnailPath,
            compressed = existing.compressed,
        )
    }

    /**
     * 썸네일 생성
     * @param imageBytes 이미지 파일 바이트
     * @param extension 파일 확장자
     * @return 썸네일 파일 경로
     */
    private fun generateThumbnail(
        imageBytes: ByteArray,
        extension: String,
    ): String {
        val thumbnailName = "${UUID.randomUUID()}_thumb.$extension"
        val thumbnailPath = uploadRoot.resolve(thumbnailName)

        validateImage(imageBytes)

        try {
            ByteArrayInputStream(imageBytes).use { input ->
                ByteArrayOutputStream().use { output ->
                    Thumbnails.of(input)
                        .size(200, 200)
                        .keepAspectRatio(true)
                        .toOutputStream(output)
                    thumbnailPath.writeBytes(output.toByteArray())
                }
            }
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.IMAGE_THUMBNAIL_GENERATION_FAILED)
        }

        return thumbnailPath.toString()
    }

    /**
     * 이미지 유효성 검사
     * @param imageBytes 이미지 파일 바이트
     */
    private fun validateImage(imageBytes: ByteArray) {
        ByteArrayInputStream(imageBytes).use { input ->
            ImageIO.read(input) ?: throw BusinessException(ErrorCode.INVALID_IMAGE_DATA)
        }
    }

    /**
     * 압축 처리
     * @param fileBytes 파일 바이트
     * @param originalName 원본 파일명
     * @param originalSize 원본 파일 크기
     * @param originalExtension 파일 확장자
     * @return 압축 결과 (최종 바이트, 압축 여부, 저장 확장자, 저장 크기)
     */
    private fun processCompression(
        fileBytes: ByteArray,
        originalName: String,
        originalExtension: String,
        originalSize: Long,
    ): CompressionResult {
        var finalBytes = fileBytes
        var compressed = false
        var storedExtension = originalExtension
        if (uncompressedExt.contains(originalExtension) && originalSize > maxSize) {
            try {
                val compressedBytes = zipCompress(fileBytes, originalName)
                val compressionRatio = calculateCompressionRatio(originalSize, compressedBytes.size.toLong())
                if (compressionRatio > 0.1) {
                    finalBytes = compressedBytes
                    compressed = true
                    storedExtension = "zip"
                }
            } catch (e: Exception) {
                throw BusinessException(ErrorCode.FILE_COMPRESSION_FAILED)
            }
        }

        val storedSize = finalBytes.size.toLong()
        return CompressionResult(finalBytes, compressed, storedExtension, storedSize)
    }

    /**
     * 압축 비율 계산
     * @param originalSize 원본 파일 크기
     * @param compressedSize 압축된 파일 크기
     * @return 압축 비율 (0.0 ~ 1.0)
     */
    private fun calculateCompressionRatio(
        originalSize: Long,
        compressedSize: Long,
    ): Double {
        return 1.0 - (compressedSize.toDouble() / originalSize)
    }

    /**
     * 파일 확장자 추출
     * @param fileName 파일명
     * @return 파일 확장자
     */
    private fun extractExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot > 0) fileName.substring(lastDot + 1) else ""
    }

    /**
     * 파일 해시 계산 (SHA-256)
     * @param bytes 파일 바이트
     * @return 해시 문자열
     */
    private fun computeFileHash(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }

    /**
     * ZIP 압축
     * @param srcBytes 압축할 파일 바이트
     * @param entryName ZIP 내의 엔트리 이름
     * @return 압축된 파일 바이트
     */
    private fun zipCompress(
        srcBytes: ByteArray,
        entryName: String,
    ): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zos ->
            val entry = ZipEntry(entryName)
            zos.putNextEntry(entry)
            zos.write(srcBytes)
            zos.closeEntry()
        }
        return bos.toByteArray()
    }

    /**
     * ZIP 압축 해제 (단일 파일)
     * @param zipBytes ZIP 파일 바이트
     * @return 압축 해제된 파일 바이트
     */
    private fun unzipSingleFile(zipBytes: ByteArray): ByteArray {
        ByteArrayInputStream(zipBytes).use { bais ->
            ZipInputStream(bais).use { zis ->
                val entry = zis.nextEntry ?: throw BusinessException(ErrorCode.FILE_DECOMPRESSION_FAILED)
                val buffer = ByteArrayOutputStream()
                zis.copyTo(buffer)
                zis.closeEntry()
                return buffer.toByteArray()
            }
        }
    }

    private data class CompressionResult(
        val finalBytes: ByteArray,
        val compressed: Boolean,
        val storedExtension: String,
        val storedSize: Long,
    )
}
