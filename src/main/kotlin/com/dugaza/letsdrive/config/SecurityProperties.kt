package com.dugaza.letsdrive.config

import com.dugaza.letsdrive.entity.user.Role
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "security")
class SecurityProperties {
    var permitAll: List<String> = mutableListOf()
    var roleMappings: List<RoleMapping> = mutableListOf()
    var defaultRole: String = Role.USER.name
}

data class RoleMapping(
    val roles: List<String> = emptyList(),
    val urls: List<String> = emptyList(),
)
