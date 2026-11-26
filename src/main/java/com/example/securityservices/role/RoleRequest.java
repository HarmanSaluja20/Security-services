package com.example.securityservices.role;

import com.example.securityservices.user.AppRole;
import com.example.securityservices.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "role_requests")
public class RoleRequest {
    public enum Status { PENDING, APPROVED, REJECTED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private AppUser user;
    @ManyToOne(optional = false) @JoinColumn(name = "requested_role_id")
    private AppRole requestedRole;
    @Column(nullable = false, length = 500)
    private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status = Status.PENDING;
    @ManyToOne @JoinColumn(name = "reviewed_by")
    private AppUser reviewedBy;
    @Column(name = "reviewed_at")
    private Instant reviewedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    protected RoleRequest() {}
    public RoleRequest(AppUser user, AppRole role, String reason) {
        this.user = user; this.requestedRole = role; this.reason = reason;
    }
    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public AppRole getRequestedRole() { return requestedRole; }
    public String getReason() { return reason; }
    public Status getStatus() { return status; }
    public void review(AppUser reviewer, Status decision) {
        if (status != Status.PENDING) throw new IllegalStateException("Request already reviewed");
        if (decision == Status.PENDING) throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        status = decision; reviewedBy = reviewer; reviewedAt = Instant.now();
        if (decision == Status.APPROVED) user.addRole(requestedRole);
    }
}
