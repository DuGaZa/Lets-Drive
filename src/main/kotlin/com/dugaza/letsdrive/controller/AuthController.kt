package com.dugaza.letsdrive.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/auth")
class AuthController {
    @GetMapping("/users/login")
    fun login(): String {
        return "login-page"
    }
}
