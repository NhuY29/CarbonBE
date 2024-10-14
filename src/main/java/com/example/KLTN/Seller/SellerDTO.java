package com.example.KLTN.Seller;

import com.example.KLTN.Entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SellerDTO {
    private UUID sellerId;
    private UUID userId;
    private String companyName;
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
}
