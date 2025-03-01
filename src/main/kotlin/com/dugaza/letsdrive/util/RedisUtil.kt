package com.dugaza.letsdrive.util

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisUtil(
    private val template: StringRedisTemplate,
) {
    fun setValueExpire(
        key: String,
        value: String,
        duration: Long,
        unit: TimeUnit = TimeUnit.SECONDS,
    ) {
        template.opsForValue().set(key, value, duration, unit)
    }

    fun getValue(key: String): String? {
        return template.opsForValue().get(key)
    }

    fun setHashValueExpire(
        key: String,
        dataMap: Map<String, String>,
        duration: Long,
        unit: TimeUnit = TimeUnit.SECONDS,
    ) {
        template.opsForHash<String, String>().putAll(key, dataMap)
        template.expire(key, duration, unit)
    }

    fun getHashValue(
        key: String,
        vararg hashKeys: String,
    ): Map<String, String>? {
        return hashKeys.zip(template.opsForHash<String, String>().multiGet(key, hashKeys.toList()))
            .toMap()
    }

    fun addSet(
        key: String,
        value: String,
    ) {
        template.opsForSet().add(key, value)
    }

    fun getSet(key: String): Set<String>? {
        return template.opsForSet().members(key)
    }

    fun removeSetMember(
        key: String,
        value: String,
    ) {
        template.opsForSet().remove(key, value)
    }

    fun delete(key: String) {
        template.delete(key)
    }
}
