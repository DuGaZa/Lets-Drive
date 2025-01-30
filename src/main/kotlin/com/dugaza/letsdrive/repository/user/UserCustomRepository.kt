package com.dugaza.letsdrive.repository.user

import com.dugaza.letsdrive.entity.user.User
import java.util.UUID

interface UserCustomRepository {
    fun findUserByUserId(userId: UUID): User?
}
