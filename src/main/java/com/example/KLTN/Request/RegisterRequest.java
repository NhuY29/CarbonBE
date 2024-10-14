package com.example.KLTN.Request;

import com.example.KLTN.Buyer.BuyerDTO;
import com.example.KLTN.DTO.UserDTO;
import com.example.KLTN.Seller.SellerDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private UserDTO userDTO;
    private BuyerDTO buyerDTO;
    private SellerDTO sellerDTO;
}
