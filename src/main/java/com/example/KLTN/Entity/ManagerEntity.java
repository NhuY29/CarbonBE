package com.example.KLTN.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "Manager")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "User", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID managerId;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column
    private String role;
}
