package com.example.KLTN.Trade;

import com.example.KLTN.projectManagement.ProjectEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "trade2")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Trade2Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "trade2_id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID trade2Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "field")
    private String field;


    @Column(name = "quantity")
    private int quantity;

    @Column(name = "mint_token")
    private String mintToken;

    @Column(name = "standard_id", columnDefinition = "BINARY(16)")
    private UUID standardId;

    @Column(name = "type_id", columnDefinition = "BINARY(16)")
    private UUID typeId;

    @Column(name = "project_description")
    private String projectDescription;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "standard_name")
    private String standardName;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;

}
