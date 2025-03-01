package com.dugaza.letsdrive.service

import com.dugaza.letsdrive.entity.base.BaseEntity
import com.dugaza.letsdrive.entity.file.FileDetail
import com.dugaza.letsdrive.entity.file.FileMaster
import com.dugaza.letsdrive.entity.user.User
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.repository.file.FileDetailRepository
import com.dugaza.letsdrive.repository.file.FileMasterRepository
import com.dugaza.letsdrive.service.file.FileService
import com.dugaza.letsdrive.service.user.UserService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.springframework.web.multipart.MultipartFile
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional
import java.util.UUID
import javax.imageio.ImageIO

class FileServiceTest {
    @TempDir
    lateinit var tempDir: Path

    private lateinit var fileDetailRepository: FileDetailRepository
    private lateinit var fileMasterRepository: FileMasterRepository
    private lateinit var userService: UserService
    private lateinit var fileService: FileService

    @BeforeEach
    fun setUp() {
        fileDetailRepository = mockk(relaxed = true)
        fileMasterRepository = mockk(relaxed = true)
        userService = mockk(relaxed = true)
        fileService = FileService(fileDetailRepository, fileMasterRepository, userService)

        // uploadRoot를 임시 디렉토리로 재설정
        val uploadRootField = FileService::class.java.getDeclaredField("uploadRoot")
        uploadRootField.isAccessible = true
        uploadRootField.set(fileService, tempDir)
    }

    // --- 헬퍼 함수 ---

    /**
     * 지정된 확장자(format)를 사용하여 [width] x [height] 크기의 유효한 이미지를 생성.
     */
    private fun createValidImage(
        width: Int,
        height: Int,
        color: Color,
        format: String,
    ): ByteArray {
        val bufferedImage =
            BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).apply {
                createGraphics().apply {
                    this.color = color
                    fillRect(0, 0, width, height)
                    dispose()
                }
            }
        val baos = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, format, baos)
        return baos.toByteArray()
    }

    private fun createValidJpgImage(
        width: Int,
        height: Int,
        color: Color,
    ): ByteArray = createValidImage(width, height, color, "jpg")

    private fun createValidBmpImage(
        width: Int,
        height: Int,
        color: Color,
    ): ByteArray = createValidImage(width, height, color, "bmp")

    /**
     * [BaseEntity]를 상속하는 엔티티의 private id 필드에 [id]를 할당.
     */
    private fun setEntityId(
        entity: BaseEntity,
        id: UUID,
    ) {
        val idField = BaseEntity::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(entity, id)
    }

    /**
     * 더미 User 반환
     */
    private fun dummyUser(): User = mockk(relaxed = true)

    // --- 테스트: uploadFile ---
    @Test
    fun `uploadFile should create FileMaster and FileDetail when a valid jpg file is uploaded`() {
        val userId = UUID.randomUUID()
        val dummyUser = dummyUser()
        every { userService.getUserById(userId) } returns dummyUser

        val fileMaster = FileMaster(dummyUser)
        every { fileMasterRepository.save(any<FileMaster>()) } returns fileMaster

        val jpgBytes = createValidJpgImage(100, 100, Color.RED)
        val multipartFile =
            mockk<MultipartFile>(relaxed = true).apply {
                every { originalFilename } returns "test.jpg"
                every { contentType } returns "image/jpeg"
                every { bytes } returns jpgBytes
            }

        every { fileDetailRepository.findByFileHash(any()) } returns null
        every { fileDetailRepository.saveAll(any<List<FileDetail>>()) } answers { firstArg() }

        val (masterResult, detailsResult) = fileService.uploadFile(userId, listOf(multipartFile))

        assertEquals(fileMaster, masterResult)
        assertEquals(1, detailsResult.size)

        val detail = detailsResult.first()
        assertEquals("test.jpg", detail.originalName)
        assertEquals("jpg", detail.originalExtension)
        assertEquals("image/jpeg", detail.mimeType)
        assertNotNull(detail.thumbnailPath, "썸네일 경로가 생성되어야 함")

        val storedFile = tempDir.resolve(detail.storedName).toFile()
        assertTrue(storedFile.exists(), "파일이 저장되어야 함")
        assertArrayEquals(jpgBytes, storedFile.readBytes())

        // 썸네일 파일 존재 여부 확인
        val thumbnailFile = tempDir.resolve(detail.thumbnailPath!!).toFile()
        assertTrue(thumbnailFile.exists(), "썸네일 파일이 저장되어야 함")
    }

    @Test
    fun `uploadFile should compress bmp file if size exceeds maxSize`() {
        val userId = UUID.randomUUID()
        val dummyUser = dummyUser()
        every { userService.getUserById(userId) } returns dummyUser

        val fileMaster = FileMaster(dummyUser)
        every { fileMasterRepository.save(any<FileMaster>()) } returns fileMaster

        // maxSize를 낮게 설정하여 압축 조건 강제 (10바이트 초과)
        val maxSizeField = FileService::class.java.getDeclaredField("maxSize")
        maxSizeField.isAccessible = true
        maxSizeField.set(fileService, 10L)

        val bmpBytes = createValidBmpImage(1000, 1000, Color.RED)
        val multipartFile =
            mockk<MultipartFile>(relaxed = true).apply {
                every { originalFilename } returns "test.bmp"
                every { contentType } returns "image/bmp"
                every { bytes } returns bmpBytes
            }

        every { fileDetailRepository.findByFileHash(any()) } returns null
        every { fileDetailRepository.saveAll(any<List<FileDetail>>()) } answers { firstArg() }

        val (_, details) = fileService.uploadFile(userId, listOf(multipartFile))
        val detail = details.first()

        assertTrue(detail.compressed, "압축되어야 함")
        assertEquals("zip", detail.storedExtension, "압축 파일은 'zip' 확장자로 저장되어야 함")

        val storedFile = tempDir.resolve(detail.storedName).toFile()
        assertTrue(storedFile.exists(), "압축 파일이 저장되어야 함")

        // FileDetail의 id에 임의 UUID 할당 후 downloadFile 테스트
        val generatedId = UUID.randomUUID()
        setEntityId(detail, generatedId)
        every { fileDetailRepository.findById(generatedId) } returns Optional.of(detail)
        val downloadedBytes = fileService.downloadFile(generatedId)
        assertArrayEquals(bmpBytes, downloadedBytes, "압축해제된 파일은 원본 BMP와 동일해야 함")
    }

    @Test
    fun `uploadFile should throw an exception when uploading an unsupported extension`() {
        val userId = UUID.randomUUID()
        val dummyUser = dummyUser()
        every { userService.getUserById(userId) } returns dummyUser

        val fileMaster = FileMaster(dummyUser)
        every { fileMasterRepository.save(any<FileMaster>()) } returns fileMaster

        val multipartFile =
            mockk<MultipartFile>(relaxed = true).apply {
                every { originalFilename } returns "test.txt"
                every { contentType } returns "text/plain"
                every { bytes } returns "test".toByteArray()
            }

        val exception =
            assertThrows<BusinessException> {
                fileService.uploadFile(userId, listOf(multipartFile))
            }
        assertEquals(ErrorCode.INVALID_EXTENSION, exception.errorCode)
    }

    @Test
    fun `uploadFile should throw an exception when uploading invalid image data`() {
        val userId = UUID.randomUUID()
        val dummyUser = dummyUser()
        every { userService.getUserById(userId) } returns dummyUser

        val fileMaster = FileMaster(dummyUser)
        every { fileMasterRepository.save(any<FileMaster>()) } returns fileMaster

        val multipartFile =
            mockk<MultipartFile>(relaxed = true).apply {
                every { originalFilename } returns "test.jpg"
                every { contentType } returns "image/jpeg"
                every { bytes } returns "test".toByteArray() // 올바른 이미지 데이터가 아님
            }

        every { fileDetailRepository.findByFileHash(any()) } returns null

        val exception =
            assertThrows<BusinessException> {
                fileService.uploadFile(userId, listOf(multipartFile))
            }
        assertEquals(ErrorCode.INVALID_IMAGE_DATA, exception.errorCode)
    }

    // --- 테스트: downloadFile ---
    @Test
    fun `downloadFile should throw an exception when decompression fails`() {
        val dummyDetail =
            FileDetail(
                fileMaster = FileMaster(dummyUser()),
                originalName = "test.jpg",
                storedName = "test.zip",
                storedPath = tempDir.resolve("test.zip").toString(),
                originalSize = 100,
                storedSize = 50,
                originalExtension = "jpg",
                storedExtension = "zip",
                mimeType = "image/jpeg",
                fileHash = "test",
                compressed = true,
            )
        val generatedId = UUID.randomUUID()
        setEntityId(dummyDetail, generatedId)
        every { fileDetailRepository.findById(generatedId) } returns Optional.of(dummyDetail)
        Files.write(tempDir.resolve("test.zip"), "invalid zip content".toByteArray())

        val exception =
            assertThrows<BusinessException> {
                fileService.downloadFile(generatedId)
            }
        assertEquals(ErrorCode.FILE_DECOMPRESSION_FAILED, exception.errorCode)
    }

    @Test
    fun `getFileDetail should thorw an exception when the file detail does not exist`() {
        val nonExistentId = UUID.randomUUID()
        every { fileDetailRepository.findById(nonExistentId) } returns Optional.empty()

        val exception =
            assertThrows<BusinessException> {
                fileService.getFileDetail(nonExistentId)
            }
        assertEquals(ErrorCode.NOT_FOUND_FILE_DETAIL, exception.errorCode)
    }

    // --- 테스트: getDefaultImage ---
    @Test
    fun `getDefaultImage should throw an exception when default image detail does not exist`() {
        val defaultImageDetailIdField = FileService::class.java.getDeclaredField("defaultImageDetailId")
        defaultImageDetailIdField.isAccessible = true
        val defaultImageDetailId = defaultImageDetailIdField.get(fileService) as UUID

        every { fileDetailRepository.findById(defaultImageDetailId) } returns Optional.empty()
        val userId = UUID.randomUUID()

        val exception =
            assertThrows<BusinessException> {
                fileService.getDefaultImage(userId)
            }
        assertEquals(ErrorCode.NOT_FOUND_FILE_DETAIL, exception.errorCode)
    }

    @Test
    fun `getDefaultImage should create a copy of the default image and return a FileDetail`() {
        val defaultImageDetailIdField = FileService::class.java.getDeclaredField("defaultImageDetailId")
        defaultImageDetailIdField.isAccessible = true
        val defaultImageDetailId = defaultImageDetailIdField.get(fileService) as UUID

        val defaultDetail =
            FileDetail(
                fileMaster = FileMaster(dummyUser()),
                originalName = "default.jpg",
                storedName = "default.jpg",
                storedPath = tempDir.resolve("default.jpg").toString(),
                originalSize = 100,
                storedSize = 100,
                originalExtension = "jpg",
                storedExtension = "jpg",
                mimeType = "image/jpeg",
                fileHash = "default",
                compressed = false,
            )
        setEntityId(defaultDetail, UUID.randomUUID())
        every { fileDetailRepository.findById(defaultImageDetailId) } returns Optional.of(defaultDetail)
        every { fileMasterRepository.save(any<FileMaster>()) } answers { firstArg() }
        every { fileDetailRepository.save(any<FileDetail>()) } returnsArgument 0

        every { userService.getUserById(any()) } returns dummyUser()

        val fileMasterResult = fileService.getDefaultImage(UUID.randomUUID())
        verify {
            fileDetailRepository.save(
                match {
                    it.storedName == defaultDetail.storedName && it.originalName == defaultDetail.originalName
                },
            )
        }
        assertNotNull(fileMasterResult)
    }

    // --- 테스트: 중복 파일 처리 ---
    @Test
    fun `uploadFile should return new FileDetail and not store a file when a file with the same hash`() {
        val userId = UUID.randomUUID()
        val dummyUser = dummyUser()
        every { userService.getUserById(userId) } returns dummyUser

        val fileMaster = FileMaster(dummyUser)
        every { fileMasterRepository.save(any<FileMaster>()) } returns fileMaster

        val jpgBytes = createValidJpgImage(100, 100, Color.RED)
        val multipartFile =
            mockk<MultipartFile>(relaxed = true).apply {
                every { originalFilename } returns "duplicate.jpg"
                every { contentType } returns "image/jpeg"
                every { bytes } returns jpgBytes
            }

        val existingDetail =
            FileDetail(
                fileMaster = fileMaster,
                originalName = "existing.jpg",
                storedName = "existing_path.jpg",
                storedPath = tempDir.resolve("existing_path.jpg").toString(),
                originalSize = jpgBytes.size.toLong(),
                storedSize = jpgBytes.size.toLong(),
                originalExtension = "jpg",
                storedExtension = "jpg",
                mimeType = "image/jpeg",
                fileHash = "duplicate",
                compressed = false,
            )
        setEntityId(existingDetail, UUID.randomUUID())

        every { fileDetailRepository.findByFileHash(any()) } returns existingDetail
        every { fileDetailRepository.saveAll(any<List<FileDetail>>()) } answers { firstArg() }

        val (_, details) = fileService.uploadFile(userId, listOf(multipartFile))
        val detail = details.first()
        assertEquals(existingDetail.storedName, detail.storedName)
    }
}
