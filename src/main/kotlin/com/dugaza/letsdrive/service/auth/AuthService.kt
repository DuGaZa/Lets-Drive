package com.dugaza.letsdrive.service.auth

import com.dugaza.letsdrive.entity.user.AuthProvider
import com.dugaza.letsdrive.entity.user.Role
import com.dugaza.letsdrive.entity.user.User
import com.dugaza.letsdrive.service.file.FileService
import com.dugaza.letsdrive.service.user.UserService
import com.dugaza.letsdrive.util.generateRandomNickname
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val fileService: FileService,
    private val userService: UserService,
) {
    @Transactional
    fun signup(
        provider: AuthProvider,
        providerId: String,
    ): User {
        val user =
            userService.createUser(
                User(
                    provider = provider,
                    providerId = providerId,
                    nickname = generateRandomNickname(),
                ),
            )
        user.addRole(Role.UNVERIFIED_USER)

        val fileMaster = fileService.getDefaultImage(user.id!!)
        user.changeProfileImage(fileMaster)

        return user
    }

    fun login(
        provider: AuthProvider,
        providerId: String,
    ): User {
        val user = userService.getUserByProvider(provider, providerId)
        user.login()
        return user
    }
}
