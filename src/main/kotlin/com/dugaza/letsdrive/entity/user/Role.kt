package com.dugaza.letsdrive.entity.user

enum class Role(val roleName: String) {
    USER("유저"),
    UNVERIFIED_USER("이메일 미인증 유저"),
    ADMIN("관리자"),
}
