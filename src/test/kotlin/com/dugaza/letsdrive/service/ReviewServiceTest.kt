package com.dugaza.letsdrive.service

import com.dugaza.letsdrive.config.FileProperties
import com.dugaza.letsdrive.dto.review.GetReviewListRequest
import com.dugaza.letsdrive.dto.review.ReviewCreateRequest
import com.dugaza.letsdrive.entity.common.Review
import com.dugaza.letsdrive.entity.common.evaluation.Evaluation
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationAnswer
import com.dugaza.letsdrive.entity.common.evaluation.EvaluationQuestion
import com.dugaza.letsdrive.entity.course.Course
import com.dugaza.letsdrive.entity.file.FileMaster
import com.dugaza.letsdrive.entity.user.AuthProvider
import com.dugaza.letsdrive.entity.user.User
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.repository.course.CourseRepository
import com.dugaza.letsdrive.repository.evaluation.EvaluationAnswerRepository
import com.dugaza.letsdrive.repository.evaluation.EvaluationQuestionRepository
import com.dugaza.letsdrive.repository.evaluation.EvaluationRepository
import com.dugaza.letsdrive.repository.evaluation.EvaluationResultRepository
import com.dugaza.letsdrive.repository.file.FileDetailRepository
import com.dugaza.letsdrive.repository.file.FileMasterRepository
import com.dugaza.letsdrive.repository.review.ReviewRepository
import com.dugaza.letsdrive.repository.user.UserRepository
import com.dugaza.letsdrive.service.auth.TokenService
import com.dugaza.letsdrive.service.course.CourseService
import com.dugaza.letsdrive.service.evaluation.EvaluationService
import com.dugaza.letsdrive.service.file.FileService
import com.dugaza.letsdrive.service.mail.MailService
import com.dugaza.letsdrive.service.review.ReviewService
import com.dugaza.letsdrive.service.user.UserService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ReviewServiceTest {
    @MockK
    private lateinit var fileProperties: FileProperties

    @MockK
    private lateinit var reviewRepository: ReviewRepository

    @MockK
    private lateinit var courseRepository: CourseRepository

    @MockK
    private lateinit var evaluationRepository: EvaluationRepository

    @MockK
    private lateinit var evaluationResultRepository: EvaluationResultRepository

    @MockK
    private lateinit var evaluationAnswerRepository: EvaluationAnswerRepository

    @MockK
    private lateinit var evaluationQuestionRepository: EvaluationQuestionRepository

    @MockK
    private lateinit var fileDetailRepository: FileDetailRepository

    @MockK
    private lateinit var fileMasterRepository: FileMasterRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private var mailService: MailService = mockk()

    @InjectMockKs
    private var tokenService: TokenService = mockk()

    @InjectMockKs
    private var courseService: CourseService = mockk()

    @InjectMockKs
    private var evaluationService: EvaluationService = mockk()

    @InjectMockKs
    private var reviewService: ReviewService = mockk()

    @InjectMockKs
    private var fileService: FileService = mockk()

    @InjectMockKs
    private var userService: UserService = mockk()

    private lateinit var mockCourse: Course
    private lateinit var courseId: UUID
    private lateinit var mockUser: User
    private lateinit var userId: UUID
    private lateinit var userProviderId: String
    private lateinit var mockReview: Review
    private lateinit var reviewId: UUID
    private lateinit var mockEvaluation: Evaluation
    private lateinit var evaluationId: UUID
    private lateinit var mockCourseReviewCreateRequest: ReviewCreateRequest
    private lateinit var mockCourseEvaluationQuestion1: EvaluationQuestion
    private lateinit var mockCourseEvaluationQuestion1Answer1: EvaluationAnswer
    private lateinit var mockCourseEvaluationQuestion1Answer2: EvaluationAnswer
    private lateinit var mockCourseEvaluationQuestion1Answer3: EvaluationAnswer
    private lateinit var mockCourseEvaluationQuestion2: EvaluationQuestion
    private lateinit var mockCourseEvaluationQuestion2Answer1: EvaluationAnswer
    private lateinit var mockCourseEvaluationQuestion2Answer2: EvaluationAnswer
    private lateinit var mockCourseEvaluationQuestion2Answer3: EvaluationAnswer
    private lateinit var courseEvaluationQuestion1Id: UUID
    private lateinit var courseEvaluationQuestion1Answer1Id: UUID
    private lateinit var courseEvaluationQuestion1Answer2Id: UUID
    private lateinit var courseEvaluationQuestion1Answer3Id: UUID
    private lateinit var courseEvaluationQuestion2Id: UUID
    private lateinit var courseEvaluationQuestion2Answer1Id: UUID
    private lateinit var courseEvaluationQuestion2Answer2Id: UUID
    private lateinit var courseEvaluationQuestion2Answer3Id: UUID
    private lateinit var mockFileMaster: FileMaster
    private lateinit var fileMasterId: UUID

    private var userEmail = "mock@example.com"
    private val evaluationType = "TestEvaluation Type"
    private val reviewScore = 2.5

    @BeforeEach
    fun setUp() {
        fileProperties = FileProperties()
        userService =
            UserService(
                userRepository = userRepository,
                mailService = mailService,
                tokenService = tokenService,
            )
        courseService =
            CourseService(
                courseRepository = courseRepository,
            )
        evaluationService =
            EvaluationService(
                evaluationRepository = evaluationRepository,
                evaluationResultRepository = evaluationResultRepository,
                evaluationAnswerRepository = evaluationAnswerRepository,
                evaluationQuestionRepository = evaluationQuestionRepository,
            )
        fileService =
            FileService(
                fileDetailRepository = fileDetailRepository,
                fileMasterRepository = fileMasterRepository,
                userService = userService,
            )
        reviewService =
            ReviewService(
                reviewRepository = reviewRepository,
                userService = userService,
                courseService = courseService,
                evaluationService = evaluationService,
                fileService = fileService,
            )

        fileProperties = FileProperties()
        courseId = UUID.randomUUID()
        userId = UUID.randomUUID()
        userProviderId = UUID.randomUUID().toString()
        reviewId = UUID.randomUUID()
        evaluationId = UUID.randomUUID()
        courseEvaluationQuestion1Id = UUID.randomUUID()
        courseEvaluationQuestion1Answer1Id = UUID.randomUUID()
        courseEvaluationQuestion1Answer2Id = UUID.randomUUID()
        courseEvaluationQuestion1Answer3Id = UUID.randomUUID()
        courseEvaluationQuestion2Id = UUID.randomUUID()
        courseEvaluationQuestion2Answer1Id = UUID.randomUUID()
        courseEvaluationQuestion2Answer2Id = UUID.randomUUID()
        courseEvaluationQuestion2Answer3Id = UUID.randomUUID()
        fileMasterId = UUID.randomUUID()

        mockUser =
            mockk<User> {
                every { id } returns userId
                every { email } returns userEmail
                every { provider } returns AuthProvider.GOOGLE
                every { providerId } returns userProviderId
                every { nickname } returns "TEST_USER_NICKNAME"
            }

        mockCourse =
            mockk<Course> {
                every { id } returns courseId
                every { user } returns mockUser
                every { name } returns "TEST_USER_NAME"
            }

        mockEvaluation =
            mockk<Evaluation> {
                every { id } returns evaluationId
                every { type } returns evaluationType
            }

        mockCourseEvaluationQuestion1 =
            mockk<EvaluationQuestion> {
                every { id } returns courseEvaluationQuestion1Id
                every { evaluation } returns mockEvaluation
                every { question } returns "TEST_COURSE_EVALUATION_QUESTION1"
            }

        mockCourseEvaluationQuestion1Answer1 =
            mockk<EvaluationAnswer> {
                every { id } returns courseEvaluationQuestion1Answer1Id
                every { question } returns mockCourseEvaluationQuestion1
                every { answer } returns "TEST_COURSE_EVALUATION_QUESTION1_ANSWER1"
            }

        mockCourseEvaluationQuestion1Answer2 =
            mockk<EvaluationAnswer> {
                every { id } returns courseEvaluationQuestion1Answer2Id
                every { question } returns mockCourseEvaluationQuestion1
                every { answer } returns "TEST_COURSE_EVALUATION_QUESTION1_ANSWER2"
            }

        mockCourseEvaluationQuestion1Answer3 =
            mockk<EvaluationAnswer> {
                every { id } returns courseEvaluationQuestion1Answer3Id
                every { question } returns mockCourseEvaluationQuestion1
                every { answer } returns "TEST_COURSE_EVALUATION_QUESTION1_ANSWER3"
            }

        mockCourseEvaluationQuestion2 =
            mockk<EvaluationQuestion> {
                every { id } returns courseEvaluationQuestion2Id
                every { evaluation } returns mockEvaluation
                every { question } returns "TEST_COURSE_EVALUATION_QUESTION2"
            }

        mockCourseEvaluationQuestion2Answer1 =
            mockk<EvaluationAnswer> {
                every { id } returns courseEvaluationQuestion2Answer1Id
                every { question } returns mockCourseEvaluationQuestion2
                every { answer } returns "TEST_COURSE_EVALUATION_QUESTION2_ANSWER1"
            }

        mockCourseEvaluationQuestion2Answer2 =
            mockk<EvaluationAnswer> {
                every { id } returns courseEvaluationQuestion2Answer2Id
                every { question } returns mockCourseEvaluationQuestion2
                every { answer } returns "TEST_COURSE_EVALUATION_QUESTION2_ANSWER2"
            }

        mockCourseEvaluationQuestion2Answer3 =
            mockk<EvaluationAnswer> {
                every { id } returns courseEvaluationQuestion2Answer3Id
                every { question } returns mockCourseEvaluationQuestion2
                every { answer } returns "TEST_COURSE_EVALUATION_QUESTION2_ANSWER3"
            }

        every {
            evaluationAnswerRepository.findById(any<UUID>())
        } answers {
            val id = firstArg<UUID>()
            when (id) {
                courseEvaluationQuestion1Answer1Id -> Optional.of(mockCourseEvaluationQuestion1Answer1)
                courseEvaluationQuestion1Answer2Id -> Optional.of(mockCourseEvaluationQuestion1Answer2)
                courseEvaluationQuestion1Answer3Id -> Optional.of(mockCourseEvaluationQuestion1Answer3)
                courseEvaluationQuestion2Answer1Id -> Optional.of(mockCourseEvaluationQuestion2Answer1)
                courseEvaluationQuestion2Answer2Id -> Optional.of(mockCourseEvaluationQuestion2Answer2)
                courseEvaluationQuestion2Answer3Id -> Optional.of(mockCourseEvaluationQuestion2Answer3)
                else -> Optional.empty()
            }
        }

        val realCourseReviewCreateRequest =
            ReviewCreateRequest(
                targetId = courseId,
                userId = userId,
                evaluationId = evaluationId,
                targetType = TargetType.COURSE,
                evaluationResultList =
                    arrayOf(
                        courseEvaluationQuestion2Answer1Id,
                        courseEvaluationQuestion1Answer2Id,
                    ).toList(),
                fileMasterId = fileMasterId,
                score = reviewScore,
                content = "TEST_CONTENT",
            )
        mockCourseReviewCreateRequest = spyk(realCourseReviewCreateRequest)

        mockReview =
            mockk<Review> {
                every { id } returns reviewId
                every { targetId } returns courseId
                every { user } returns mockUser
                every { evaluation } returns mockEvaluation
                every { score } returns reviewScore
                every { content } returns "TEST_CONTENT"
                every { isDisplayed } returns true
            }

        mockFileMaster =
            mockk<FileMaster> {
                every { id } returns fileMasterId
                every { user } returns mockUser
            }
    }

    @Nested
    @DisplayName("checkExistsTarget(): ")
    inner class CheckExistsTarget {
        @Test
        @DisplayName("타겟 ID를 이용하여 해당 타겟이 존재 확인 테스트 성공")
        fun `should successfully verify target existence using target id`() {
            // Given
            val targetType = TargetType.COURSE

            every {
                courseRepository.existsById(courseId)
            } returns true

            val courseService = CourseService(courseRepository)
            val reviewService =
                ReviewService(reviewRepository, userService, courseService, evaluationService, fileService)

            // When & Then
            assertDoesNotThrow {
                reviewService.checkExistsTarget(
                    targetId = courseId,
                    targetType = targetType,
                )
            }
        }

        @Test
        @DisplayName("유효하지 않은 타겟 ID를 이용하여 엔티티 존재 확인 시 예외 발생")
        fun `should throw exception when verifying entity existence with invalid target id`() {
            // Given
            val invalidUUID = UUID.randomUUID()
            val targetType = TargetType.COURSE

            every {
                courseRepository.existsById(invalidUUID)
            } returns false

            val courseService = CourseService(courseRepository)
            val reviewService =
                ReviewService(reviewRepository, userService, courseService, evaluationService, fileService)

            // When
            val exception =
                assertThrows<BusinessException> {
                    reviewService.checkExistsTarget(
                        targetId = invalidUUID,
                        targetType = targetType,
                    )
                }

            // Then
            assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("checkValidScore(): ")
    inner class CheckValidScore {
        @Test
        @DisplayName("유효한 스코어를 이용하여 스코어가 정상적인 값인지 테스트 성공")
        fun `should return true when given valid score`() {
            // Given
            val validScoreList: List<Double> =
                arrayOf(
                    0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0,
                ).toList()

            // When & Then
            assertDoesNotThrow {
                validScoreList.stream().forEach {
                    reviewService.checkValidScore(it)
                }
            }
        }

        @Test
        @DisplayName("유효하지 않은 스코어를 이용하여 스코어가 정상적인 값인지 체크 예외 발생")
        fun `should throw exception when given invalid score`() {
            // Given
            val invalidScoreList: List<Double> =
                arrayOf(
                    -0.5, -20.0, -3.277E43, 0.0, -0.0, -0.201291,
                    0.1, 0.2391, 4.7, 8.5, 10.0, 15.5555, 2.39E42,
                ).toList()

            // When & Then
            invalidScoreList.forEach {
                val exception =
                    assertThrows<BusinessException> {
                        reviewService.checkValidScore(it)
                    }

                assertEquals(ErrorCode.REVIEW_SCORE_INVALID, exception.errorCode)
            }
        }
    }

    @Nested
    @DisplayName("getReviewById(): ")
    inner class GetReviewById {
        @Test
        @DisplayName("유효한 리뷰 ID를 이용하여 리뷰 엔티티 조회 테스트 성공")
        fun `should successfully retrieve review entity using valid review id`() {
            // Given
            every {
                reviewRepository.findById(reviewId)
            } returns Optional.of(mockReview)

            // When
            val result =
                assertDoesNotThrow<Review> {
                    reviewService.getReviewById(reviewId)
                }

            // Then
            assertEquals(reviewId, result.id)
        }

        @Test
        @DisplayName("유효하지 않은 리뷰 ID를 이용해 엔티티 조회 시 예외 발생")
        fun `should throw exception when retrieving review entity with invalid id`() {
            // Given
            val invalidUUID = UUID.randomUUID()
            every {
                reviewRepository.findById(invalidUUID)
            } returns Optional.empty()

            // When
            val exception =
                assertThrows<BusinessException> {
                    reviewService.getReviewById(invalidUUID)
                }

            // Then
            assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("checkValidEvaluationQuestionByAnswerId(): ")
    inner class CheckValidEvaluationQuestionByAnswerId {
        @Test
        @DisplayName("평가에 등록된 유효한 답변을 사용하여 테스트 통과")
        fun `should pass with valid answer registered in evaluation`() {
            // Given
            val validUUID = UUID.randomUUID()

            every {
                evaluationAnswerRepository.findById(validUUID)
            } returns
                Optional.of(
                    mockk<EvaluationAnswer> {
                        every { id } returns validUUID
                        every { answer } returns "VALID_ANSWER"
                        every { question } returns
                            mockk<EvaluationQuestion> {
                                every { id } returns UUID.randomUUID()
                                every { question } returns "TEST_QUESTION"
                                every { evaluation } returns mockEvaluation
                            }
                    },
                )

            // When & Then
            assertDoesNotThrow {
                reviewService.checkValidEvaluationQuestionByAnswerId(
                    evaluation = mockEvaluation,
                    answerId = validUUID,
                )
            }
        }

        @Test
        @DisplayName("평가에 등록되지 않은 답변을 사용하여 테스트 시 예외 발생")
        fun `should throw exception when using unregistered answer`() {
            // Given
            val invalidUUID = UUID.randomUUID()

            every {
                evaluationAnswerRepository.findById(invalidUUID)
            } returns
                Optional.of(
                    mockk<EvaluationAnswer> {
                        every { id } returns invalidUUID
                        every { answer } returns "INVALID_ANSWER"
                        every { question } returns
                            mockk<EvaluationQuestion> {
                                every { id } returns UUID.randomUUID()
                                every { question } returns "TEST_QUESTION"
                                every { evaluation } returns
                                    mockk<Evaluation> {
                                        every { id } returns UUID.randomUUID()
                                    }
                            }
                    },
                )

            // When
            val exception =
                assertThrows<BusinessException> {
                    reviewService.checkValidEvaluationQuestionByAnswerId(
                        evaluation = mockEvaluation,
                        answerId = invalidUUID,
                    )
                }

            // Then
            assertEquals(ErrorCode.INVALID_EVALUATION_ANSWER, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("createReview(): ")
    inner class CreateReview {
        @Nested
        @DisplayName("Target 관련 테스트")
        inner class Target {
            @Test
            @DisplayName("유효한 타겟 ID를 이용하여 리뷰 생성 테스트 성공")
            fun `should create review successfully when given valid target ID`() {
                // Given
                // COURSE
                val targetType = TargetType.COURSE
                val targetId = courseId

                when (targetType) {
                    TargetType.COURSE ->
                        every {
                            courseRepository.existsById(targetId)
                        } returns true

                    else -> {
                        fail()
                    }
                }

                every {
                    userRepository.findUserById(userId)
                } returns mockUser

                every {
                    evaluationRepository.find(
                        id = evaluationId
                    )
                } returns mockEvaluation

                every {
                    fileMasterRepository.findById(fileMasterId)
                } returns Optional.of(mockFileMaster)

                every {
                    reviewRepository.save(
                        match {
                            it.targetId == mockCourseReviewCreateRequest.targetId &&
                                it.user.id == mockCourseReviewCreateRequest.userId &&
                                it.evaluation.id == mockCourseReviewCreateRequest.evaluationId &&
                                it.score == mockCourseReviewCreateRequest.score &&
                                it.file.id == mockCourseReviewCreateRequest.fileMasterId
                        },
                    )
                } returns mockReview

                every {
                    evaluationResultRepository.exists(
                        reviewId = any(),
                        questionId = any(),
                        userId = any(),
                    )
                } answers {
                    val innerAnswerId = secondArg<UUID>()
                    when (innerAnswerId) {
                        courseEvaluationQuestion1Id -> false
                        courseEvaluationQuestion2Id -> false
                        else -> true
                    }
                }

                every {
                    evaluationResultRepository.save(
                        match {
                            it.review.id == reviewId
                        },
                    )
                } returns mockk()

                // When
                val result =
                    assertDoesNotThrow<Review> {
                        reviewService.createReview(mockCourseReviewCreateRequest)
                    }

                // Then
                assertEquals(reviewId, result.id)
                assertEquals(courseId, result.targetId)
            }

            @Test
            @DisplayName("유효하지 않은 타겟 ID를 이용하여 리뷰 생성 테스트 시 예외 발생")
            fun `should throw exception when given invalid target ID`() {
                // Given
                val invalidCourseUUID = UUID.randomUUID()
                val dtoSpy = spyk<ReviewCreateRequest>(mockCourseReviewCreateRequest)

                every {
                    dtoSpy.targetId
                } returns invalidCourseUUID

                every {
                    userRepository.findUserById(userId)
                } returns mockUser

                every {
                    evaluationRepository.find(
                        id = evaluationId
                    )
                } returns mockEvaluation

                every {
                    fileMasterRepository.findById(fileMasterId)
                } returns Optional.of(mockFileMaster)

                every {
                    courseRepository.findById(invalidCourseUUID)
                } returns Optional.empty()

                every {
                    courseRepository.existsById(invalidCourseUUID)
                } returns false

                // When
                val exception =
                    assertThrows<BusinessException> {
                        reviewService.createReview(dtoSpy)
                    }

                // Then
                assertEquals(ErrorCode.COURSE_NOT_FOUND, exception.errorCode)
            }
        }

        @Nested
        @DisplayName("User 관련 테스트")
        inner class User {
            @Test
            @DisplayName("유효한 유저 ID를 이용하여 리뷰 생성 테스트 성공")
            fun `should create review successfully when given valid user ID`() {
                // Given
                every {
                    userRepository.findUserById(userId)
                } returns mockUser

                every {
                    evaluationRepository.find(
                        id = evaluationId
                    )
                } returns mockEvaluation

                every {
                    fileMasterRepository.findById(fileMasterId)
                } returns Optional.of(mockFileMaster)

                every {
                    courseRepository.findById(courseId)
                } returns Optional.of(mockCourse)

                every {
                    courseRepository.existsById(courseId)
                } returns true

                every {
                    reviewRepository.save(
                        match {
                            it.targetId == mockCourseReviewCreateRequest.targetId &&
                                it.user.id == mockCourseReviewCreateRequest.userId &&
                                it.evaluation.id == mockCourseReviewCreateRequest.evaluationId &&
                                it.score == mockCourseReviewCreateRequest.score &&
                                it.file.id == mockCourseReviewCreateRequest.fileMasterId
                        },
                    )
                } returns mockReview

                every {
                    evaluationResultRepository.exists(
                        reviewId = any(),
                        questionId = any(),
                        userId = any(),
                    )
                } answers {
                    val innerAnswerId = secondArg<UUID>()
                    when (innerAnswerId) {
                        courseEvaluationQuestion1Id -> false
                        courseEvaluationQuestion2Id -> false
                        else -> true
                    }
                }

                every {
                    evaluationResultRepository.save(
                        match {
                            it.review.id == reviewId
                        },
                    )
                } returns mockk()

                // When
                val result =
                    assertDoesNotThrow<Review> {
                        reviewService.createReview(mockCourseReviewCreateRequest)
                    }

                // Then
                assertEquals(reviewId, result.id)
                assertEquals(userId, result.user.id)
            }

            @Test
            @DisplayName("유효하지 않은 유저 ID를 이용하여 리뷰 생성 테스트 시 예외 발생")
            fun `should throw exception when given invalid user ID`() {
                // Given
                val invalidUserUUID = UUID.randomUUID()

                val dtoSpy = spyk<ReviewCreateRequest>(mockCourseReviewCreateRequest)
                every {
                    dtoSpy.userId
                } returns invalidUserUUID

                every {
                    userRepository.findUserById(invalidUserUUID)
                } returns null

                // When
                val exception =
                    assertThrows<BusinessException> {
                        reviewService.createReview(dtoSpy)
                    }

                // Then
                assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
            }
        }

        @Nested
        @DisplayName("Evaluation 관련 테스트")
        inner class EvaluationTest {
            @Test
            @DisplayName("유효한 평가 ID를 이용하여 리뷰 생성 테스트 성공")
            fun `should create review successfully when given valid evaluation ID`() {
                // Given
                every {
                    userRepository.findUserById(userId)
                } returns mockUser

                every {
                    evaluationRepository.find(
                        id = evaluationId
                    )
                } returns mockEvaluation

                every {
                    fileMasterRepository.findById(fileMasterId)
                } returns Optional.of(mockFileMaster)

                every {
                    courseRepository.findById(courseId)
                } returns Optional.of(mockCourse)

                every {
                    courseRepository.existsById(courseId)
                } returns true

                every {
                    reviewRepository.save(
                        match {
                            it.targetId == mockCourseReviewCreateRequest.targetId &&
                                it.user.id == mockCourseReviewCreateRequest.userId &&
                                it.evaluation.id == mockCourseReviewCreateRequest.evaluationId &&
                                it.score == mockCourseReviewCreateRequest.score &&
                                it.file.id == mockCourseReviewCreateRequest.fileMasterId
                        },
                    )
                } returns mockReview

                every {
                    evaluationResultRepository.exists(
                        reviewId = any(),
                        questionId = any(),
                        userId = any(),
                    )
                } answers {
                    val innerAnswerId = secondArg<UUID>()
                    when (innerAnswerId) {
                        courseEvaluationQuestion1Id -> false
                        courseEvaluationQuestion2Id -> false
                        else -> true
                    }
                }

                every {
                    evaluationResultRepository.save(
                        match {
                            it.review.id == reviewId
                        },
                    )
                } returns mockk()

                // When
                val result =
                    assertDoesNotThrow<Review> {
                        reviewService.createReview(mockCourseReviewCreateRequest)
                    }

                // Then
                assertEquals(reviewId, result.id)
                assertEquals(evaluationId, result.evaluation.id)
            }

            @Test
            @DisplayName("유효하지 않은 평가 ID를 이용하여 리뷰 생성 테스트 시 예외 발생")
            fun `should throw exception when given invalid evaluation ID`() {
                // Given
                val invalidEvaluationUUID = UUID.randomUUID()
                val dtoSpy = spyk<ReviewCreateRequest>(mockCourseReviewCreateRequest)

                every {
                    dtoSpy.evaluationId
                } returns invalidEvaluationUUID

                every {
                    userRepository.findUserById(userId)
                } returns mockUser

                every {
                    evaluationRepository.find(
                        id = invalidEvaluationUUID
                    )
                } returns null

                // When
                val exception =
                    assertThrows<BusinessException> {
                        reviewService.createReview(dtoSpy)
                    }

                // Then
                assertEquals(ErrorCode.EVALUATION_NOT_FOUND, exception.errorCode)
            }
        }

        @Nested
        @DisplayName("EvaluationResult List 관련 테스트")
        inner class EvaluationResult {
            @Test
            @DisplayName("유효한 평가 결과 목록을 이용하여 리뷰 생성 테스트 성공")
            fun `should create review successfully when given valid evaluation result list`() {
                // Given
                val spyDto = spyk<ReviewCreateRequest>(mockCourseReviewCreateRequest)
                val mockEvaluationResultList =
                    arrayOf(
                        courseEvaluationQuestion1Answer2Id,
                        courseEvaluationQuestion2Answer1Id,
                    ).toList()

                every {
                    spyDto.evaluationResultList
                } returns mockEvaluationResultList

                every {
                    userRepository.findUserById(userId)
                } returns mockUser

                every {
                    evaluationRepository.find(
                        id = evaluationId
                    )
                } returns mockEvaluation

                every {
                    fileMasterRepository.findById(fileMasterId)
                } returns Optional.of(mockFileMaster)

                every {
                    courseRepository.findById(courseId)
                } returns Optional.of(mockCourse)

                every {
                    courseRepository.existsById(courseId)
                } returns true

                every {
                    reviewRepository.save(
                        match {
                            it.targetId == mockCourseReviewCreateRequest.targetId &&
                                it.user.id == mockCourseReviewCreateRequest.userId &&
                                it.evaluation.id == mockCourseReviewCreateRequest.evaluationId &&
                                it.score == mockCourseReviewCreateRequest.score &&
                                it.file.id == mockCourseReviewCreateRequest.fileMasterId
                        },
                    )
                } returns mockReview

                every {
                    evaluationResultRepository.exists(
                        reviewId = any(),
                        questionId = any(),
                        userId = any(),
                    )
                } answers {
                    val innerQuestionId = secondArg<UUID>()
                    when (innerQuestionId) {
                        courseEvaluationQuestion1Id -> false
                        courseEvaluationQuestion2Id -> false
                        else -> true
                    }
                }

                every {
                    evaluationResultRepository.save(
                        match {
                            it.review.id == reviewId
                        },
                    )
                } returns
                    mockk {
                        every { id } returns UUID.randomUUID()
                        every { review } returns mockReview
                        every { user } returns mockUser
                    }

                // When
                val result =
                    assertDoesNotThrow<Review> {
                        reviewService.createReview(mockCourseReviewCreateRequest)
                    }

                // Then
                assertEquals(reviewId, result.id)
            }

            @Test
            @DisplayName("유효하지 않은 평가 결과 목록을 이용하여 리뷰 생성 테스트시 예외 발생 (중복 답변)")
            fun `should throw exception when evaluation result list contains duplicate answers`() {
                // Given
                val spyDto = spyk<ReviewCreateRequest>(mockCourseReviewCreateRequest)
                val invalidEvaluationResultList =
                    arrayOf(
                        courseEvaluationQuestion1Answer1Id,
                        courseEvaluationQuestion1Answer1Id,
                    ).toList()

                every {
                    spyDto.evaluationResultList
                } returns invalidEvaluationResultList

                every {
                    userRepository.findUserById(userId)
                } returns mockUser

                every {
                    evaluationRepository.find(
                        id = evaluationId
                    )
                } returns mockEvaluation

                every {
                    fileMasterRepository.findById(fileMasterId)
                } returns Optional.of(mockFileMaster)

                every {
                    courseRepository.findById(courseId)
                } returns Optional.of(mockCourse)

                every {
                    courseRepository.existsById(courseId)
                } returns true

                every {
                    reviewRepository.save(
                        match {
                            it.targetId == mockCourseReviewCreateRequest.targetId &&
                                it.user.id == mockCourseReviewCreateRequest.userId &&
                                it.evaluation.id == mockCourseReviewCreateRequest.evaluationId &&
                                it.score == mockCourseReviewCreateRequest.score &&
                                it.file.id == mockCourseReviewCreateRequest.fileMasterId
                        },
                    )
                } returns mockReview

                every {
                    evaluationAnswerRepository.findById(any<UUID>())
                } answers {
                    val innerAnswerId = firstArg<UUID>()
                    when (innerAnswerId) {
                        courseEvaluationQuestion1Answer1Id -> Optional.of(mockCourseEvaluationQuestion1Answer1)
                        else -> Optional.empty()
                    }
                }

                var counter = 0 // 호출 횟수 추적을 위한 변수
                every {
                    evaluationResultRepository.exists(
                        reviewId = any(),
                        questionId = any(),
                        userId = any(),
                    )
                } answers {
                    val innerQuestionId = secondArg<UUID>()

                    if (innerQuestionId == courseEvaluationQuestion1Id) {
                        counter++
                        return@answers when (counter) {
                            1 -> false // 첫 번째 호출
                            2 -> true // 두 번째 호출
                            else -> true // 이후 호출 기본값
                        }
                    } else {
                        false
                    }
                }

                every {
                    evaluationResultRepository.save(
                        match {
                            it.review.id == reviewId
                        },
                    )
                } returns
                    mockk {
                        every { id } returns UUID.randomUUID()
                        every { review } returns mockReview
                        every { user } returns mockUser
                    }

                // When
                val exception =
                    assertThrows<BusinessException> {
                        reviewService.createReview(spyDto)
                    }

                // Then
                assertEquals(ErrorCode.EVALUATION_RESULT_ANSWER_CONFLICT, exception.errorCode)
            }

            @Test
            @DisplayName("유효하지 않은 평가 결과 목록을 이용하여 리뷰 생성 테스트시 예외 발생 (같은 질문에 두가지 답변)")
            fun `should throw exception when evaluation result list contains multiple answers for the same question`() {
                // Given
                val spyDto = spyk<ReviewCreateRequest>(mockCourseReviewCreateRequest)
                val invalidEvaluationResultList =
                    arrayOf(
                        courseEvaluationQuestion1Answer1Id,
                        courseEvaluationQuestion1Answer2Id,
                    ).toList()

                every {
                    spyDto.evaluationResultList
                } returns invalidEvaluationResultList

                every {
                    userRepository.findUserById(userId)
                } returns mockUser

                every {
                    evaluationRepository.find(
                        id = evaluationId
                    )
                } returns mockEvaluation

                every {
                    fileMasterRepository.findById(fileMasterId)
                } returns Optional.of(mockFileMaster)

                every {
                    courseRepository.findById(courseId)
                } returns Optional.of(mockCourse)

                every {
                    courseRepository.existsById(courseId)
                } returns true

                every {
                    reviewRepository.save(
                        match {
                            it.targetId == mockCourseReviewCreateRequest.targetId &&
                                it.user.id == mockCourseReviewCreateRequest.userId &&
                                it.evaluation.id == mockCourseReviewCreateRequest.evaluationId &&
                                it.score == mockCourseReviewCreateRequest.score &&
                                it.file.id == mockCourseReviewCreateRequest.fileMasterId
                        },
                    )
                } returns mockReview

                every {
                    evaluationAnswerRepository.findById(any<UUID>())
                } answers {
                    val innerAnswerId = firstArg<UUID>()
                    when (innerAnswerId) {
                        courseEvaluationQuestion1Answer1Id -> Optional.of(mockCourseEvaluationQuestion1Answer1)
                        courseEvaluationQuestion1Answer2Id -> Optional.of(mockCourseEvaluationQuestion1Answer2)
                        else -> Optional.empty()
                    }
                }

                var counter = 0 // 호출 횟수 추적을 위한 변수
                every {
                    evaluationResultRepository.exists(
                        reviewId = any(),
                        questionId = any(),
                        userId = any(),
                    )
                } answers {
                    val innerQuestionId = secondArg<UUID>()

                    if (innerQuestionId == courseEvaluationQuestion1Id) {
                        counter++
                        return@answers when (counter) {
                            1 -> false // 첫 번째 호출
                            2 -> true // 두 번째 호출
                            else -> true // 이후 호출 기본값
                        }
                    } else {
                        false
                    }
                }

                every {
                    evaluationResultRepository.save(
                        match {
                            it.review.id == reviewId
                        },
                    )
                } returns
                    mockk {
                        every { id } returns UUID.randomUUID()
                        every { review } returns mockReview
                        every { user } returns mockUser
                    }

                // When
                val exception =
                    assertThrows<BusinessException> {
                        reviewService.createReview(spyDto)
                    }

                // Then
                assertEquals(ErrorCode.EVALUATION_RESULT_ANSWER_CONFLICT, exception.errorCode)
            }

            @Test
            @DisplayName("평가 타입에 맞지 않은 질문 답변을 이용하여 리뷰 생성 시 예외 발생")
            fun `should throw exception when evaluation result list contains mismatched question types`() {
                // Given
                val invalidEvaluationAnswer =
                    mockk<EvaluationAnswer> {
                        every { id } returns UUID.randomUUID()
                        every { question } returns
                            mockk<EvaluationQuestion> {
                                every { id } returns UUID.randomUUID()
                                every { evaluation } returns
                                    mockk<Evaluation> {
                                        every { id } returns UUID.randomUUID()
                                        every { type } returns "OTHER_TYPE"
                                    }
                                every { question } returns "INVALID_QUESTION"
                            }
                        every { answer } returns "INVALID_ANSWER"
                    }
                val spyDto =
                    spyk<ReviewCreateRequest>(mockCourseReviewCreateRequest) {
                        every { evaluationResultList } returns
                            arrayOf(
                                invalidEvaluationAnswer.id!!,
                            ).toList()
                    }

                every {
                    userRepository.findUserById(userId)
                } returns mockUser

                every {
                    evaluationRepository.find(
                        id = evaluationId
                    )
                } returns mockEvaluation

                every {
                    fileMasterRepository.findById(fileMasterId)
                } returns Optional.of(mockFileMaster)

                every {
                    courseRepository.findById(courseId)
                } returns Optional.of(mockCourse)

                every {
                    courseRepository.existsById(courseId)
                } returns true

                val invalidAnswerId = invalidEvaluationAnswer.id!!
                every {
                    evaluationAnswerRepository.findById(eq(invalidAnswerId))
                } returns Optional.of(invalidEvaluationAnswer)

                // When
                val exception =
                    assertThrows<BusinessException> {
                        reviewService.createReview(spyDto)
                    }

                // Then
                assertEquals(ErrorCode.INVALID_EVALUATION_ANSWER, exception.errorCode)
            }
        }

        @Nested
        @DisplayName("FileMaster 관련 테스트")
        inner class FileMaster {
            @Test
            @DisplayName("유효하지 않은 파일 마스터 ID를 이용하여 리뷰 생성 테스트 시 예외 발생")
            fun `should throw exception when given invalid file master ID`() {
                // Given
                val invalidFileMasterUUID = UUID.randomUUID()
                val spyDto = spyk<ReviewCreateRequest>(mockCourseReviewCreateRequest)

                every {
                    spyDto.fileMasterId
                } returns invalidFileMasterUUID

                every {
                    userRepository.findUserById(userId)
                } returns mockUser

                every {
                    evaluationRepository.find(
                        id = evaluationId
                    )
                } returns mockEvaluation

                every {
                    fileMasterRepository.findById(eq(invalidFileMasterUUID))
                } returns Optional.empty()

                // When
                val exception =
                    assertThrows<BusinessException> {
                        reviewService.createReview(spyDto)
                    }

                // Then
                assertEquals(ErrorCode.NOT_FOUND_FILE_MASTER, exception.errorCode)
            }
        }
    }

    @Nested
    @DisplayName("getReviewList(): ")
    inner class GetReviewList {
        @Test
        @DisplayName("유효한 타겟 ID, TYPE 을 이용하여 리뷰 리스트 조회 테스트 성공")
        fun `should successfully retrieve review list with valid target ID and type`() {
            // Given
            val mockGetReviewListRequest =
                mockk<GetReviewListRequest> {
                    every { targetId } returns courseId
                    every { targetType } returns TargetType.COURSE
                }

            every {
                courseRepository.existsById(courseId)
            } returns true

            every {
                reviewRepository.findAllByTargetId(courseId)
            } returns
                List(10) {
                    mockk<Review> {
                        every { targetId } returns courseId
                    }
                }

            // When
            val result =
                assertDoesNotThrow<List<Review>> {
                    reviewService.getReviewList(mockGetReviewListRequest)
                }

            // Then
            result.forEach {
                assertEquals(courseId, it.targetId)
            }
        }
    }
}
