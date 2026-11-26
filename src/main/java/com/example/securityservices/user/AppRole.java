package com.example.securityservices.user;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class AppRole {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    protected AppRole() {}
    public String getName() { return name; }
    public Long getId() { return id; }
}
