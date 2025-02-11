package com.dugaza.letsdrive.controller

import com.dugaza.letsdrive.dto.user.RandomNicknameResponse
import com.dugaza.letsdrive.entity.user.CustomOAuth2User
import com.dugaza.letsdrive.extensions.userId
import com.dugaza.letsdrive.service.user.UserService
import com.dugaza.letsdrive.util.generateRandomNickname
import com.dugaza.letsdrive.vo.user.ChangeEmail
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/random-nickname")
    fun getRandomNickname(): ResponseEntity<RandomNicknameResponse> {
        return ResponseEntity.ok(RandomNicknameResponse(generateRandomNickname()))
    }

    @PostMapping("/change-email")
    fun changeEmail(
        @AuthenticationPrincipal
        user: CustomOAuth2User,
        @Valid
        @RequestBody
        req: ChangeEmail,
    ): ResponseEntity<Void> {
        userService.changeEmail(user.userId, req.email)
        return ResponseEntity.accepted().build()
    }
}
