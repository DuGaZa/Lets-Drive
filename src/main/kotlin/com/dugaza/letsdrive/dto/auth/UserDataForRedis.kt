package com.dugaza.letsdrive.dto.auth

import com.dugaza.letsdrive.entity.user.AuthProvider
import com.dugaza.letsdrive.entity.user.Role

data class UserDataForRedis(
    val id: String,
    val roles: List<Role>,
    val provider: AuthProvider,
    val providerId: String,
    val deviceId: String,
)
