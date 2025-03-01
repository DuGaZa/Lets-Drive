package com.dugaza.letsdrive.vo.oauth2.naver

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class NaverUserInfo(
    val resultcode: String,
    val message: String,
    val response: NaverUserInfoResponse,
)
