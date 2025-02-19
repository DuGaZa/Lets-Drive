package com.dugaza.letsdrive.handler.auth

import com.dugaza.letsdrive.entity.user.CustomOAuth2User
import com.dugaza.letsdrive.extensions.userId
import com.dugaza.letsdrive.service.auth.TokenService
import com.dugaza.letsdrive.service.user.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationHandler(
    private val tokenService: TokenService,
    private val userService: UserService,
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val principal = authentication.principal as CustomOAuth2User
        val user = userService.getUserById(principal.userId)
        val deviceId = request.getHeader("X-Device-Id") ?: "${user.id}" // todo: 나중에 앱 연동하면 앱에서 헤더 추가해주기
        if (deviceId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid device id")
            return
        }
        val accessToken = tokenService.createAccessToken(user, deviceId)
        val refreshToken = tokenService.createRefreshToken(user.id!!)

        val accessCookie =
            Cookie("ACCESS_TOKEN", accessToken).apply {
                isHttpOnly = true
                secure = request.isSecure
                path = "/" // todo: 배포시 도메인으로 바꾸기
                maxAge = tokenService.accessTokenDuration.toInt()
            }
        val refreshCookie =
            Cookie("REFRESH_TOKEN", refreshToken).apply {
                isHttpOnly = true
                secure = request.isSecure
                path = "/"
                maxAge = tokenService.refreshTokenDuration.toInt()
            }
        response.addCookie(accessCookie)
        response.addCookie(refreshCookie)
        response.sendRedirect("/")
    }
}
