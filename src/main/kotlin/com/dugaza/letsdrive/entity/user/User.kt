package com.dugaza.letsdrive.entity.user

import com.dugaza.letsdrive.entity.base.BaseEntity
import com.dugaza.letsdrive.entity.file.FileMaster
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime
import org.hibernate.annotations.SQLDelete

@Entity
@Table(
    name = "user",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["provider", "provider_id"]),
    ],
)
@SQLDelete(sql = "UPDATE user SET deleted_at = NOW() WHERE id = ?")
class User(
    @Column(nullable = true, unique = true)
    var email: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: AuthProvider,
    @Column(nullable = false)
    val providerId: String,
    @Column(nullable = false)
    var nickname: String,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id", nullable = true, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    var profileImage: FileMaster? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserStatus = UserStatus.ACTIVE,
    @OneToMany(mappedBy = "user", cascade = [CascadeType.PERSIST])
    var roles: MutableSet<UserRole> = mutableSetOf(),
    @Column(nullable = true)
    var lastLoginAt: LocalDateTime? = null,
) : BaseEntity() {
    fun login() {
        lastLoginAt = LocalDateTime.now()
        if (status == UserStatus.DORMANT) {
            status = UserStatus.ACTIVE
        }
    }

    fun changeName(newName: String) {
        nickname = newName
    }

    fun changeProfileImage(fileMaster: FileMaster) {
        profileImage = fileMaster
    }

    fun changeEmail(newEmail: String) {
        email = newEmail
    }

    fun addRole(role: Role) {
        roles.add(
            UserRole(
                role = role,
                user = this,
            ),
        )
    }

    fun removeRole(role: Role) {
        val userRole = roles.find { it.role == role } ?: return
        roles.remove(userRole)
        userRole.delete()
    }

    fun existsRole(role: Role): Boolean {
        return roles.any { it.role == role }
    }

    fun toDormant() {
        status = UserStatus.DORMANT
    }

    fun withdraw() {
        status = UserStatus.WITHDRAWN
        delete()
    }
}
