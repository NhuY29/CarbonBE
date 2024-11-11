package com.example.KLTN.Withdrawal;

import com.example.KLTN.Entity.UserEntity;

public interface WithdrawalService {
    WithdrawalResponse processWithdrawal(String token, double amount, String bankName, String bankAccountNumber, String accountHolderName)  ;
}
