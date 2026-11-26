package com.example.securityservices.auth;

import com.example.securityservices.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository users;
    public JwtAuthFilter(JwtService jwtService, UserRepository users) {
        this.jwtService = jwtService; this.users = users;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String email = jwtService.getSubject(header.substring(7));
                users.findByEmail(email).filter(u -> u.isEnabled()).ifPresent(user -> {
                    var authorities = user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName())).toList();
                    var auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            } catch (RuntimeException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
