package com.example.securityservices.auth;

import com.example.securityservices.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private AppUser user;
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(nullable = false)
    private boolean revoked;
    protected RefreshToken() {}
    public RefreshToken(AppUser user, String tokenHash, Instant expiresAt) {
        this.user = user; this.tokenHash = tokenHash; this.expiresAt = expiresAt;
    }
    public AppUser getUser() { return user; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public void revoke() { revoked = true; }
}
