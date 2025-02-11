package com.dugaza.letsdrive.repository.user

import com.dugaza.letsdrive.entity.user.AuthProvider
import com.dugaza.letsdrive.entity.user.User
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID>, UserCustomRepository {
    fun findUserByEmail(email: String): User?

    @EntityGraph(attributePaths = ["roles"])
    fun findUserByProviderAndProviderId(
        provider: AuthProvider,
        providerId: String,
    ): User?

    @EntityGraph(attributePaths = ["roles"])
    fun findUserById(id: UUID): User?

    fun existsByEmail(email: String): Boolean
}
