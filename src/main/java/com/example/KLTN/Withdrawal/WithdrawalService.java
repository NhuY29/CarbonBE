package com.example.KLTN.Withdrawal;

import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Enum.Status;

import java.util.List;
import java.util.UUID;

public interface WithdrawalService {
    WithdrawalResponse processWithdrawal(String token, double amount, String bankName, String bankAccountNumber, String accountHolderName);
    List<WithdrawalDTO> getAllWithdrawals();
    List<WithdrawalDTO> getWithdrawalsByUserId(UUID userId);
    WithdrawalResponse updateWithdrawalStatus(UUID withdrawalId, Status newStatus);
}
