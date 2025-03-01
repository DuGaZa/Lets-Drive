package com.dugaza.letsdrive.controller

import com.dugaza.letsdrive.dto.auth.RefreshTokenResponse
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.service.auth.TokenService
import com.dugaza.letsdrive.service.user.UserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
@RequestMapping("/api/token")
class TokenController(
    private val tokenService: TokenService,
    private val userService: UserService,
) {
    @GetMapping("/revoke")
    fun revokeToken(
        @RequestParam token: String,
    ): ResponseEntity<Void> {
        tokenService.revokeAccessToken(token)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @RequestParam
        refreshToken: String,
        @RequestHeader
        httpHeaders: HttpHeaders,
        request: HttpServletRequest,
    ): ResponseEntity<RefreshTokenResponse> {
        val deviceId = httpHeaders.getFirst("X-Device-Id") ?: throw BusinessException(ErrorCode.INVALID_DEVICE_ID)

        return try {
            val userId = tokenService.validateRefreshToken(refreshToken) ?: throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)
            val user = userService.getUserById(userId)
            val tokens = tokenService.refreshAccessToken(user, refreshToken, deviceId)
            if (tokens != null) {
                val accessCookie =
                    ResponseCookie.from("ACCESS_TOKEN", tokens.first)
                        .httpOnly(true)
                        .secure(request.isSecure)
                        .path("/")
                        .maxAge(Duration.ofHours(tokenService.accessTokenDuration))
                        .build()

                val refreshToken =
                    ResponseCookie.from("REFRESH_TOKEN", tokens.second)
                        .httpOnly(true)
                        .secure(request.isSecure)
                        .path("/")
                        .maxAge(Duration.ofDays(tokenService.refreshTokenDuration))
                        .build()

                ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString(), refreshToken.toString())
                    .build()
            } else {
                throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)
            }
        } catch (e: BusinessException) {
            throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)
        }
    }
}
