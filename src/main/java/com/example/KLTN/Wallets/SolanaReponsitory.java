package com.example.KLTN.Wallets;

import com.example.KLTN.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolanaReponsitory extends JpaRepository<SolanaEntity, UUID> {

    Optional<SolanaEntity> findByUser(UserEntity user);

    Optional<SolanaEntity> findBySecretKey(String secretKey);
    Optional<SolanaEntity> findByPublicKey(String publicKey);
    @Query("SELECT s.secretKey FROM SolanaEntity s WHERE s.user.userId = :userId")
    Optional<String> findSecretKeyByUserId(UUID userId);

    Optional<SolanaEntity> findByUser_UserId(UUID userId);

}
