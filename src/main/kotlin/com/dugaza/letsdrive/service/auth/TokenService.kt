package com.dugaza.letsdrive.service.auth

import com.dugaza.letsdrive.dto.auth.UserDataForRedis
import com.dugaza.letsdrive.entity.user.User
import com.dugaza.letsdrive.util.RedisUtil
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class TokenService(
    @Value("\${spring.data.redis.access-token-duration}")
    val accessTokenDuration: Long,
    @Value("\${spring.data.redis.refresh-token-duration}")
    val refreshTokenDuration: Long,
    private val redisUtil: RedisUtil,
) {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Access token 생성: TTL 기본 60분
     * Redis 키: "access:{token}" → 값: userId.toString()
     */
    fun createAccessToken(
        user: User,
        deviceId: String,
    ): String {
        val token = UUID.randomUUID().toString()
        val userData =
            UserDataForRedis(
                user.id.toString(),
                user.roles.map { it.role },
                user.provider,
                user.providerId,
                deviceId,
            )
        val userDataJson = objectMapper.writeValueAsString(userData)
        redisUtil.setValueExpire("access:$token", userDataJson, accessTokenDuration, TimeUnit.HOURS)
        redisUtil.addSet("user:${user.id}:access_tokens", token)
        return token
    }

    /**
     * Refresh token 생성: TTL 기본 30일
     * Redis 키: "refresh:{token}" → 값: userId.toString()
     */
    fun createRefreshToken(userId: UUID): String {
        val token = UUID.randomUUID().toString()
        redisUtil.setValueExpire("refresh:$token", userId.toString(), refreshTokenDuration, TimeUnit.DAYS)
        return token
    }

    /**
     * Access token 검증: 유효하면 해당 userId 반환, 아니면 null
     */
    fun validateAccessToken(token: String): UserDataForRedis? {
        val userDataJson = redisUtil.getValue("access:$token")
        return if (userDataJson != null) {
            objectMapper.readValue(userDataJson, UserDataForRedis::class.java)
        } else {
            null
        }
    }

    /**
     * Refresh token 검증: 유효하면 해당 userId 반환, 아니면 null
     */
    fun validateRefreshToken(token: String): UUID? {
        return redisUtil.getValue("refresh:$token")?.let { UUID.fromString(it) }
    }

    fun revokeAccessToken(token: String) {
        val userDataJson = redisUtil.getValue("access:$token")
        if (userDataJson != null) {
            val userData = objectMapper.readValue(userDataJson, UserDataForRedis::class.java)
            redisUtil.removeSetMember("user:${userData.id}:access_tokens", token)
        }
        redisUtil.delete("access:$token")
    }

    fun revokeRefreshToken(token: String) {
        redisUtil.delete("refresh:$token")
    }

    /**
     * 사용자 정보를 기반으로, 기존 refresh token을 폐기하고 새 액세스 토큰 및 refresh token을 발급
     * 이 메서드는 순환 참조 문제를 피하기 위해 UserService를 DI하지 않고, caller에서 업데이트된 User 객체를 전달받음.
     *
     * @param user 업데이트된 사용자 객체 (예: 권한 변경 후)
     * @param refreshToken 기존 refresh token (클라이언트에서 전달)
     * @return Pair(새 access token, 새 refresh token) 또는 null (유효하지 않은 경우)
     */
    fun refreshAccessToken(
        user: User,
        refreshToken: String,
        deviceId: String,
    ): Pair<String, String>? {
        val newAccessToken = createAccessToken(user, deviceId)
        val newRefreshToken = createRefreshToken(user.id!!)

        revokeRefreshToken(refreshToken)

        return Pair(newAccessToken, newRefreshToken)
    }

    fun revokeAllTokensForUser(userId: UUID) {
        redisUtil.getSet("user:$userId:access_tokens")?.forEach {
            revokeAccessToken(it)
        }
        redisUtil.delete("user:$userId:access_tokens")
    }
}
