package com.example.KLTN.Wallets;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Contact")
public class ContactEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "contact", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID ContactId;

    private String username;
    private String walletAddress;
    @ManyToOne
    @JoinColumn(name = "wallets_id", nullable = false)
    private SolanaEntity wallet;
}
