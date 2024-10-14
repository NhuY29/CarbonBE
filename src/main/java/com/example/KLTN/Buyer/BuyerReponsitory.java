package com.example.KLTN.Buyer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BuyerReponsitory extends JpaRepository<BuyerEntity, UUID> {
}
