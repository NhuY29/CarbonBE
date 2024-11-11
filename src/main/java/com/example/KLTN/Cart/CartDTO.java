package com.example.KLTN.Cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class    CartDTO {
    private UUID cartId;
    private String tradeId;
    private String projectId;
    private String projectName;
    private String field;

    private String companyName;

    private int quantity;

    private String price;

    // Mã token mint (nếu có)
    private String mintToken;

    // ID của tiêu chuẩn
    private String standardId;

    // ID của loại giao dịch
    private String typeId;
    private String projectDescription;
    private String typeName;
    private String standardName;
    private List<String> imageUrls;
    private UUID userId;
    private String tokenAddress;
    private int amount;
}
