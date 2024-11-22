package com.example.KLTN.Withdrawal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WithdrawalRepository extends JpaRepository<WithdrawalEntity, UUID> {
    List<WithdrawalEntity> findByUser_UserId(UUID userId);
}
