package com.example.KLTN.signature;

import com.example.KLTN.projectManagement.ProjectEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "signature")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class signatureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "signature_data_url", nullable = false, columnDefinition = "TEXT")
    private String signatureDataUrl;

    @Column(name = "number_of_proposals", nullable = false)
    private int numberOfProposals;

    @Column(name = "document_number", nullable = false)
    private String documentNumber;
    @OneToOne
    @JoinColumn(name = "project_id", nullable = false, columnDefinition = "BINARY(16)")
    private ProjectEntity project;
}
