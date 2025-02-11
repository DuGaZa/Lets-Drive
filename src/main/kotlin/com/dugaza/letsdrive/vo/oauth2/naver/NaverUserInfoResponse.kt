package com.dugaza.letsdrive.vo.oauth2.naver

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class NaverUserInfoResponse(
    val id: String,
)
