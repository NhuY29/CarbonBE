package com.example.KLTN.Trade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface Trade2Repository extends JpaRepository<Trade2Entity, UUID> {
     List<Trade2Entity> findAllByUserIdAndProject_ProjectId(UUID userId, UUID projectId);

    Optional<Trade2Entity> findByMintTokenAndProject_ProjectIdAndUserId(String mintToken, UUID projectId, UUID buyerUserId);

    List<Trade2Entity> findAllByUserId(UUID userId);
}