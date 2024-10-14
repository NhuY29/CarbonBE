package com.example.KLTN.Seller;

import java.util.Optional;
import java.util.UUID;

public interface SellerService {
    SellerDTO createSeller(SellerDTO sellerDTO);


    Optional<SellerDTO> findByUserId(UUID userId);
}
