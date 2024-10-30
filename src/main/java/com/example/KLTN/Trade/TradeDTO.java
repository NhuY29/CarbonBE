package com.example.KLTN.Trade;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeDTO {
    private String projectId;
    // Tên dự án
    private String projectName;

    // Lĩnh vực giao dịch
    private String field;

    // Tên công ty thực hiện giao dịch
    private String companyName;

    // Số lượng giao dịch
    private int quantity;

    // Giá giao dịch
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
}
