package com.dugaza.letsdrive.repository.user

import com.dugaza.letsdrive.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID>, UserCustomRepository
