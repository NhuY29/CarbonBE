package com.example.KLTN.Trade;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TradeRequest {
    private UUID buyerUserId;
    private UUID projectId;
    private int quantity;
    private String mintToken;
    private String tokenAddress;
    private String price;
    private String purchasedFrom;
    private BigDecimal purchasePrice;
}
