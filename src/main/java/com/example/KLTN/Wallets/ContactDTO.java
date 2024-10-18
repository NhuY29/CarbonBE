package com.example.KLTN.Wallets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ContactDTO {
    private UUID contactId;
    private String username;
    private String walletAddress;
}
