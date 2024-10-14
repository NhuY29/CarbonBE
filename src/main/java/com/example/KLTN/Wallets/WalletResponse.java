package com.example.KLTN.Wallets;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private String publicKey;
    private String secretKey;

}
