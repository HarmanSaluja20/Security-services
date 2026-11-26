package com.example.securityservices.auth;

import com.example.securityservices.user.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

@Service
public class AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final long refreshDays;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users, RoleRepository roles, RefreshTokenRepository refreshTokens,
                       PasswordEncoder encoder, JwtService jwt,
                       @Value("${app.jwt.refresh-ttl-days:7}") long refreshDays) {
        this.users = users; this.roles = roles; this.refreshTokens = refreshTokens;
        this.encoder = encoder; this.jwt = jwt; this.refreshDays = refreshDays;
    }
    @Transactional
    public Map<String, Object> register(String email, String password, String name) {
        String normalized = email.toLowerCase().trim();
        if (users.existsByEmail(normalized)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        AppUser user = new AppUser(normalized, encoder.encode(password), name);
        user.addRole(roles.findByName("ROLE_USER").orElseThrow());
        users.save(user);
        return issue(user);
    }
    @Transactional
    public Map<String, Object> login(String email, String password) {
        AppUser user = users.findByEmail(email.toLowerCase().trim())
            .filter(AppUser::isEnabled)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (user.getPasswordHash() == null || !encoder.matches(password, user.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        return issue(user);
    }
    @Transactional
    public Map<String, Object> refresh(String rawToken) {
        RefreshToken old = refreshTokens.findByTokenHash(hash(rawToken))
            .filter(t -> !t.isRevoked() && t.getExpiresAt().isAfter(Instant.now()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        old.revoke();
        return issue(old.getUser());
    }
    @Transactional
    public void logout(String rawToken) {
        refreshTokens.findByTokenHash(hash(rawToken)).ifPresent(RefreshToken::revoke);
    }
    @Transactional
    public Map<String, Object> issueForOAuthUser(String email, String name) {
        AppUser user = users.findByEmail(email.toLowerCase()).orElseGet(() -> {
            AppUser created = new AppUser(email, null, name == null ? email : name);
            created.addRole(roles.findByName("ROLE_USER").orElseThrow());
            return users.save(created);
        });
        return issue(user);
    }
    private Map<String, Object> issue(AppUser user) {
        byte[] bytes = new byte[48]; random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshTokens.save(new RefreshToken(user, hash(raw), Instant.now().plus(refreshDays, ChronoUnit.DAYS)));
        return Map.of("accessToken", jwt.createAccessToken(user), "refreshToken", raw,
            "tokenType", "Bearer", "expiresIn", 900);
    }
    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
