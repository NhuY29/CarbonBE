package com.example.KLTN.Wallets;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface  ContactRepository extends JpaRepository<ContactEntity, UUID> {
    List<ContactEntity> findByUsernameContaining(String username);

    List<ContactEntity> findByWallet_WalletsId(UUID walletId);

}
