package com.dugaza.letsdrive.filter.auth

import com.dugaza.letsdrive.entity.user.AuthProvider
import com.dugaza.letsdrive.entity.user.CustomOAuth2User
import com.dugaza.letsdrive.service.auth.TokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

class TokenAuthenticationFilter(
    private val tokenService: TokenService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = request.cookies?.find { it.name == "ACCESS_TOKEN" }?.value
        if (token != null) {
            val userData = tokenService.validateAccessToken(token)
            if (userData != null) {
                val deviceId = request.getHeader("X-Device-Id") ?: userData.id // todo: 나중에 앱 연동하면 앱에서 헤더 추가해주기
                if (deviceId == null || userData.deviceId != deviceId) {
                    tokenService.revokeAccessToken(token)
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid device. Please re-login")
                    return
                }
                val userId = UUID.fromString(userData.id)
                val authorities = userData.roles.map { SimpleGrantedAuthority("ROLE_$it") }
                val provider = getNameAttributeKey(userData.provider)
                val customUser =
                    CustomOAuth2User(
                        authorities = authorities,
                        attributes = mapOf("userId" to userId, provider to userData.providerId),
                        nameAttributeKey = provider,
                    )
                val newAuth = UsernamePasswordAuthenticationToken(customUser, null, authorities)
                SecurityContextHolder.getContext().authentication = newAuth
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun getNameAttributeKey(provider: AuthProvider): String {
        return when (provider) {
            AuthProvider.GOOGLE -> "sub"
            AuthProvider.KAKAO -> "id"
            AuthProvider.NAVER -> "response"
        }
    }
}
