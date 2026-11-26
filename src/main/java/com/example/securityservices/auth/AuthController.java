package com.example.securityservices.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    public record RegisterRequest(@Email @NotBlank String email,
        @NotBlank @Size(min=12, max=72) String password,
        @NotBlank @Size(max=120) String displayName) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}

    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> register(@Valid @RequestBody RegisterRequest r) {
        return auth.register(r.email(), r.password(), r.displayName());
    }
    @PostMapping("/login")
    public Map<String,Object> login(@Valid @RequestBody LoginRequest r) {
        return auth.login(r.email(), r.password());
    }
    @PostMapping("/refresh")
    public Map<String,Object> refresh(@Valid @RequestBody RefreshRequest r) {
        return auth.refresh(r.refreshToken());
    }
    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequest r) { auth.logout(r.refreshToken()); }
}
