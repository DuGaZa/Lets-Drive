package com.dugaza.letsdrive.service.user

import com.dugaza.letsdrive.entity.user.AuthProvider
import com.dugaza.letsdrive.entity.user.CustomOAuth2User
import com.dugaza.letsdrive.entity.user.Role
import com.dugaza.letsdrive.exception.BusinessException
import com.dugaza.letsdrive.exception.ErrorCode
import com.dugaza.letsdrive.service.auth.AuthService
import com.dugaza.letsdrive.vo.oauth2.google.GoogleUserInfo
import com.dugaza.letsdrive.vo.oauth2.kakao.KakaoUserInfo
import com.dugaza.letsdrive.vo.oauth2.naver.NaverUserInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val authService: AuthService,
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private val objectMapper =
        ObjectMapper()
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId.lowercase()
        val provider =
            when (registrationId) {
                "google" -> AuthProvider.GOOGLE
                "naver" -> AuthProvider.NAVER
                "kakao" -> AuthProvider.KAKAO
                else -> throw BusinessException(ErrorCode.UNSUPPORTED_AUTH_PROVIDER)
            }

        val userNameAttributeName =
            userRequest.clientRegistration
                .providerDetails.userInfoEndpoint.userNameAttributeName

        val attributes = oAuth2User.attributes

        val providerId = extractUserInfo(provider, attributes)

        val user =
            try {
                authService.login(provider, providerId)
            } catch (e: BusinessException) {
                authService.signup(provider, providerId)
            }

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${Role.UNVERIFIED_USER}"))

        return CustomOAuth2User(
            authorities,
            attributes + mapOf("userId" to user.id!!),
            userNameAttributeName,
        )
    }

    private fun extractUserInfo(
        provider: AuthProvider,
        attributes: Map<String, Any>,
    ): String {
        return when (provider) {
            AuthProvider.GOOGLE -> {
                val googleUserInfo = objectMapper.convertValue(attributes, GoogleUserInfo::class.java)
                googleUserInfo.sub
            }
            AuthProvider.NAVER -> {
                val naverUserInfo = objectMapper.convertValue(attributes, NaverUserInfo::class.java)
                naverUserInfo.response.id
            }
            AuthProvider.KAKAO -> {
                val kakaoUserInfo = objectMapper.convertValue(attributes, KakaoUserInfo::class.java)
                kakaoUserInfo.id
            }
        }
    }
}
