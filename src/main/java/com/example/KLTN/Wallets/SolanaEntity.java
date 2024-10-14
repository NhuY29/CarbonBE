package com.example.KLTN.Wallets;

import com.example.KLTN.Entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolanaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "wallets_id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID walletsId;

    @Column(name = "publicKey", nullable = false)
    private String publicKey;

    @Column(name = "secretKey", nullable = false)
    private String secretKey;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

}
