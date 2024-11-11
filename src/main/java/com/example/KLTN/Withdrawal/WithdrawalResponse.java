package com.example.KLTN.Withdrawal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawalResponse {
    private String message;
    private String status;
}
