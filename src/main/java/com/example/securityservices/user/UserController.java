package com.example.securityservices.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository users;
    public UserController(UserRepository users) { this.users = users; }
    @GetMapping("/me")
    public Map<String,Object> me(Authentication authentication) {
        AppUser u = users.findByEmail(authentication.getName()).orElseThrow();
        return Map.of("id", u.getId(), "email", u.getEmail(), "displayName", u.getDisplayName(),
            "roles", u.getRoles().stream().map(AppRole::getName).collect(Collectors.toSet()));
    }
}
