package com.example.KLTN.Trade;

import com.example.KLTN.projectManagement.ProjectEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "trade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "trade_id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID tradeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "field")
    private String field;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price")
    private String price;

    @Column(name = "mintToken")
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
    @Column(name = "token_address")
    private String tokenAddress;
    @Column(name = "status")
    private String status;
}
