package com.example.KLTN.Buyer;


import com.example.KLTN.Enum.BuyerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuyerDTO {
    private UUID buyerId;
    private UUID userId;
    private BuyerType buyerType;
    private String fullName;
    private String address;
    private String phone;
    private String organizationName;
    private String personalId;
}
