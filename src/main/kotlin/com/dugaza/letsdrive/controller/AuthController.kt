package com.dugaza.letsdrive.controller

import com.dugaza.letsdrive.service.auth.TokenService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/auth")
class AuthController(
    private val tokenService: TokenService,
) {
    @GetMapping("/users/login")
    fun login(): String {
        return "login-page"
    }

    @GetMapping("/users/logout")
    fun logout(request: HttpServletRequest): ResponseEntity<Void> {
        request.cookies.find { it.name == "ACCESS_TOKEN" }
            ?. apply { tokenService.revokeAccessToken(value) }
        request.cookies.find { it.name == "REFRESH_TOKEN" }
            ?. apply { tokenService.revokeRefreshToken(value) }
        return ResponseEntity.noContent().build()
    }
}
