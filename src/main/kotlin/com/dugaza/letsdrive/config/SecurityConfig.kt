package com.dugaza.letsdrive.config

import com.dugaza.letsdrive.filter.auth.TokenAuthenticationFilter
import com.dugaza.letsdrive.handler.auth.CustomAuthenticationHandler
import com.dugaza.letsdrive.service.auth.TokenService
import com.dugaza.letsdrive.service.user.CustomOAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val corsConfigurationSource: CorsConfigurationSource,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val customAuthenticationHandler: CustomAuthenticationHandler,
    private val tokenService: TokenService,
) {
    @Bean
    fun tokenAuthenticationFilter(): TokenAuthenticationFilter {
        return TokenAuthenticationFilter(tokenService)
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors ->
                cors.configurationSource(corsConfigurationSource)
            }
            .sessionManagement { sessionManagement ->
                sessionManagement
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(
                        "/",
                        "/api/auth/users/login",
                        "/error",
                        "/api/mail/verify-email",
                        "/api/token/refresh",
                    ).permitAll()
                    .requestMatchers(
                        "/api/users/random-nickname",
                        "/api/files/default-profile-image",
                        "/api/users/change-email",
                    ).hasAnyRole("USER", "UNVERIFIED_USER")
                    .requestMatchers(
                        "/api/admin/**",
                    ).hasRole("ADMIN")
                    .anyRequest().hasRole("USER")
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .loginPage("/api/auth/users/login")
                    .userInfoEndpoint { userInfo ->
                        userInfo
                            .userService(customOAuth2UserService)
                    }
                    .successHandler { request, response, authentication ->
                        customAuthenticationHandler.onAuthenticationSuccess(request, response, authentication)
                    }
            }
            .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .logout { logout ->
                logout.logoutRequestMatcher(AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/")
                    .permitAll()
            }
            .csrf {
                it.disable()
            }
        return http.build()
    }
}
