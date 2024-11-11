package com.example.KLTN.Cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface CartReponsitory extends JpaRepository<CartEntity, UUID> {
    List<CartEntity> findByUser_UserId(UUID userId);

    List<CartEntity> findByUser_UserIdAndTradeId(UUID userId, UUID tradeId);
}
