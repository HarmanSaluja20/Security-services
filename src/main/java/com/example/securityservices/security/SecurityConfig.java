package com.example.securityservices.security;

import com.example.securityservices.auth.AuthService;
import com.example.securityservices.auth.JwtAuthFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import java.io.IOException;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtFilter,
                                              AuthService authService) throws Exception {
        AuthenticationSuccessHandler oauthSuccess = (request, response, authentication) -> {
            OAuth2User principal = (OAuth2User) authentication.getPrincipal();
            String email = principal.getAttribute("email");
            String name = principal.getAttribute("name");
            if (email == null) { response.sendError(401, "OAuth provider did not return an email"); return; }
            var tokens = authService.issueForOAuthUser(email, name);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(tokens));
        };
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/auth/**", "/oauth2/**", "/login/**", "/error").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2Login(o -> o.successHandler(oauthSuccess))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
