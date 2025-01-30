package com.dugaza.letsdrive.service

import com.dugaza.letsdrive.entity.user.User
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.repository.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
) {
    fun getUserById(userId: UUID): User {
        return userRepository.findById(userId).orElseThrow {
            BusinessException(ErrorCode.USER_NOT_FOUND)
        }
    }
}
