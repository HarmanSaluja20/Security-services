package com.example.securityservices.user;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "password_hash")
    private String passwordHash;
    @Column(name = "display_name", nullable = false)
    private String displayName;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<AppRole> roles = new HashSet<>();

    protected AppUser() {}
    public AppUser(String email, String passwordHash, String displayName) {
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public boolean isEnabled() { return enabled; }
    public Set<AppRole> getRoles() { return roles; }
    public void addRole(AppRole role) { roles.add(role); }
}
