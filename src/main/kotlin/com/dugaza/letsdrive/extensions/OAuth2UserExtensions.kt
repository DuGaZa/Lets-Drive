package com.dugaza.letsdrive.extensions

import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.UUID

val OAuth2User.userId: UUID
    get() = attributes["userId"] as UUID
