package com.example.KLTN.Wallets;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class TransferRequest {
    private String senderSecretKeyBase58;
    private String receiverPublicKey;
    private double amount;

}
