package com.example.securityservices.role;

import com.example.securityservices.user.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RoleController {
    private final RoleRepository roles;
    private final UserRepository users;
    private final RoleRequestRepository requests;
    public RoleController(RoleRepository roles, UserRepository users, RoleRequestRepository requests) {
        this.roles = roles; this.users = users; this.requests = requests;
    }
    public record RequestBody(@NotBlank String role, @NotBlank @Size(max=500) String reason) {}
    public record ReviewBody(@NotBlank String decision) {}

    @GetMapping("/roles")
    public List<String> roles() { return roles.findAll().stream().map(AppRole::getName).toList(); }

    @PostMapping("/role-requests") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> request(@Valid @RequestBody RequestBody body, Authentication auth) {
        AppUser user = users.findByEmail(auth.getName()).orElseThrow();
        AppRole role = roles.findByName(normalize(body.role())).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        if (user.getRoles().contains(role)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Already has this role");
        RoleRequest saved = requests.save(new RoleRequest(user, role, body.reason()));
        return Map.of("id", saved.getId(), "status", saved.getStatus());
    }

    @GetMapping("/admin/role-requests")
    public List<Map<String,Object>> pending() {
        return requests.findAllByOrderByCreatedAtDesc().stream().map(r -> Map.<String,Object>of(
            "id", r.getId(), "userEmail", r.getUser().getEmail(),
            "requestedRole", r.getRequestedRole().getName(), "reason", r.getReason(),
            "status", r.getStatus())).toList();
    }

    @PatchMapping("/admin/role-requests/{id}")
    @Transactional
    public Map<String,Object> review(@PathVariable Long id, @Valid @RequestBody ReviewBody body,
                                     Authentication auth) {
        RoleRequest r = requests.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        RoleRequest.Status status;
        try { status = RoleRequest.Status.valueOf(body.decision().toUpperCase()); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be APPROVED or REJECTED"); }
        if (status == RoleRequest.Status.PENDING) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be APPROVED or REJECTED");
        AppUser reviewer = users.findByEmail(auth.getName()).orElseThrow();
        r.review(reviewer, status);
        return Map.of("id", r.getId(), "status", r.getStatus());
    }
    private String normalize(String role) {
        String value = role.trim().toUpperCase();
        return value.startsWith("ROLE_") ? value : "ROLE_" + value;
    }
}
