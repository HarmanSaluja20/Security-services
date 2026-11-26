package com.example.securityservices.role;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoleRequestRepository extends JpaRepository<RoleRequest, Long> {
    List<RoleRequest> findAllByOrderByCreatedAtDesc();
}
