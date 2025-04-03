package com.dugaza.letsdrive.entity.user

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.DefaultOAuth2User

class CustomOAuth2User(
    authorities: Collection<GrantedAuthority>,
    private val attributes: Map<String, Any>,
    private val nameAttributeKey: String,
) : DefaultOAuth2User(authorities, attributes, nameAttributeKey) {
    companion object {
        private const val serialVersionUID = 1L
    }

    fun getNameAttributeKey(): String = nameAttributeKey

    fun hasRole(role: Role): Boolean {
        return this.authorities.contains(SimpleGrantedAuthority("ROLE_" + role.name))
    }
}
