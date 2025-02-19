package com.dugaza.letsdrive.service.user

import com.dugaza.letsdrive.entity.file.FileMaster
import com.dugaza.letsdrive.entity.user.AuthProvider
import com.dugaza.letsdrive.entity.user.Role
import com.dugaza.letsdrive.entity.user.User
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.repository.user.UserRepository
import com.dugaza.letsdrive.service.auth.TokenService
import com.dugaza.letsdrive.service.mail.MailService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val mailService: MailService,
    private val tokenService: TokenService,
) {
    fun getUserById(userId: UUID): User {
        return userRepository.findUserById(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }

    @Transactional
    fun createUser(user: User): User {
        user.login()
        return userRepository.save(user)
    }

    @Transactional
    fun changeProfileImage(
        userId: UUID,
        fileMaster: FileMaster,
    ) {
        val user = getUserById(userId)
        user.changeProfileImage(fileMaster)
    }

    fun getUserByProvider(
        provider: AuthProvider,
        providerId: String,
    ): User {
        return userRepository.findUserByProviderAndProviderId(provider, providerId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }

    fun changeEmail(
        userId: UUID,
        email: String,
    ) {
        val user =
            getUserById(userId)
                .apply {
                    if (this.email == email) {
                        throw BusinessException(ErrorCode.EMAIL_DUPLICATION)
                    } else {
                        if (userRepository.existsByEmail(email)) {
                            throw BusinessException(ErrorCode.EMAIL_DUPLICATION)
                        }
                    }
                }
        mailService.sendVerificationMail(userId, user.nickname, email)
    }

    @Transactional
    fun verifyMail(token: String): String? {
        val (userId, email) = mailService.verifyMail(token) ?: return null
        val user = getUserById(UUID.fromString(userId))

        user.changeEmail(email)

        if (user.existsRole(Role.UNVERIFIED_USER)) {
            user.removeRole(Role.UNVERIFIED_USER)
            user.addRole(Role.USER)

            tokenService.revokeAllTokensForUser(user.id!!)
        }

        return user.nickname
    }
}
