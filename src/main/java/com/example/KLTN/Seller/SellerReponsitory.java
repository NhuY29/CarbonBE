package com.example.KLTN.Seller;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface SellerReponsitory extends JpaRepository<SellerEntity, UUID> {
    Optional<SellerEntity> findByUser_UserId(UUID userId);

}
