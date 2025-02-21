package com.dugaza.letsdrive.dto.auth

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
