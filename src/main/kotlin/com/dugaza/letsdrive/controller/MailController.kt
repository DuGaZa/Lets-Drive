package com.dugaza.letsdrive.controller

import com.dugaza.letsdrive.service.user.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/api/mail")
class MailController(
    private val userService: UserService,
) {
    @GetMapping("/verify-email")
    fun verifyMail(
        @RequestParam("token") token: String,
        model: Model,
    ): String {
        val success = userService.verifyMail(token)

        return if (success != null) {
            model.addAttribute("nickname", success)
            "verify-email-success"
        } else {
            "verify-email-fail"
        }
    }
}
