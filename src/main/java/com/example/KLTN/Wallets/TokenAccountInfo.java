package com.example.KLTN.Wallets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TokenAccountInfo {
    private String address;
    private String mint;
    private String owner;
    private int tokenBalance;
}
