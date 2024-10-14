package com.example.KLTN.projectManagement;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "project_participant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectParticipantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "project_id", columnDefinition = "BINARY(16)")
    private UUID projectId;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;
}
