package com.example.KLTN.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetailsDTO {
    private String txnRef;
    private String amount;
    private String bankCode;
    private String bankTranNo;
    private String cardType;
    private String orderInfo;
    private String username;
    private String payDate;
    private String transactionNo;
    private String transactionStatus;
    private String tmnCode;
    private String responseCode;
}
