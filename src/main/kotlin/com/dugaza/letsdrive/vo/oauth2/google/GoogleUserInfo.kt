package com.dugaza.letsdrive.vo.oauth2.google

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleUserInfo(
    val sub: String,
)
