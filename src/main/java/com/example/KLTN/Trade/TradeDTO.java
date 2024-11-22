package com.example.KLTN.Trade;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeDTO {
    private String tradeId;
    private String projectId;
    private String projectName;
    private String field;

    private String companyName;

    private int quantity;

    private String price;

    private String mintToken;

    private String standardId;

    private String typeId;
    private String projectDescription;
    private String typeName;
    private String standardName;
    private List<String> imageUrls;
    private UUID userId;
    private String tokenAddress;
    private String status;
    private  String approvalStatus;
    private  String purchasedFrom;
    private String purchasePrice;

}
