package com.example.KLTN.Entity;

import com.example.KLTN.projectManagement.ProjectEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;


import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "User")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "firstname")
    private String firstname;
    @Column(name = "lastname")
    private String lastname;
    @Column(name = "roles")
    private String roles;
    @Column (name = "status")
    private boolean status;
    @Column (name = "isDelete")
    private boolean isDelete;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectEntity> projects;

    public UserEntity(UUID userId) {
        this.userId = userId;
    }
}