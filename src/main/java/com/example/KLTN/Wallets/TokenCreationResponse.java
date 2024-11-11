package com.example.KLTN.Wallets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenCreationResponse {
    private String mintToken;
    private String tokenAddress;
}
