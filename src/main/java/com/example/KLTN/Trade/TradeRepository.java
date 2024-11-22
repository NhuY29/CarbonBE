package com.example.KLTN.Trade;

import com.example.KLTN.projectManagement.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface  TradeRepository extends JpaRepository<TradeEntity, UUID> {
    List<TradeEntity> findAllByUserId(UUID userId);
}
