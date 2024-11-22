package com.example.KLTN.Withdrawal;

import com.example.KLTN.DTO.UserDTO;
import com.example.KLTN.Enum.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class WithdrawalDTO {
    private UUID withdrawalId;
    private double amount;
    private String bankName;
    private String bankAccountNumber;
    private String accountHolderName;
    private LocalDateTime requestTime;
    private LocalDateTime approvalTime;
    private String remainingTime;
    private Status status;
    private UserDTO user;
    private String transactionSignature;

    public WithdrawalDTO(UUID withdrawalId, double amount, String bankName, String bankAccountNumber,
                         String accountHolderName, LocalDateTime requestTime, LocalDateTime approvalTime,
                         String remainingTime, Status status, UserDTO userDTO, String transactionSignature) {
        this.withdrawalId = withdrawalId;
        this.amount = amount;
        this.bankName = bankName;
        this.bankAccountNumber = bankAccountNumber;
        this.accountHolderName = accountHolderName;
        this.requestTime = requestTime;
        this.approvalTime = approvalTime;
        this.remainingTime = remainingTime;
        this.status = status;
        this.user = userDTO;
        this.transactionSignature = transactionSignature;
    }
}
