package com.javacraftacademy.userservice.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, length = 20)
    private RoleName name;
    
    @Size(max = 200)
    private String description;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
    
    public enum RoleName {
        ROLE_USER,
        ROLE_MODERATOR,
        ROLE_ADMIN,
        ROLE_INSTRUCTOR,
        ROLE_STUDENT,
        ROLE_PREMIUM_USER
    }
    
    // Constructeurs
    public Role() {}
    
    public Role(RoleName name) {
        this.name = name;
    }
    
    public Role(RoleName name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public RoleName getName() {
        return name;
    }
    
    public void setName(RoleName name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Set<User> getUsers() {
        return users;
    }
    
    public void setUsers(Set<User> users) {
        this.users = users;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return id != null && id.equals(role.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name=" + name +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
