package com.example.KLTN.Withdrawal;

import com.example.KLTN.Enum.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/withdrawal")
public class WithdrawalController {
    @Autowired
    private WithdrawalService withdrawalService;
    @GetMapping("/all")
    public List<WithdrawalDTO> getAllWithdrawals() {
        return withdrawalService.getAllWithdrawals();
    }
    @GetMapping("/user/{userId}")
    public List<WithdrawalDTO> getWithdrawalsByUserId(@PathVariable UUID userId) {
        return withdrawalService.getWithdrawalsByUserId(userId);
    }
    @PutMapping("/update-status/{withdrawalId}")
    public ResponseEntity<WithdrawalResponse> updateWithdrawalStatus(
            @PathVariable UUID withdrawalId,
            @RequestParam Status newStatus) {
        try {
            WithdrawalResponse response = withdrawalService.updateWithdrawalStatus(withdrawalId, newStatus);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            WithdrawalResponse errorResponse = new WithdrawalResponse("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @PostMapping("/request")
    public ResponseEntity<WithdrawalResponse> requestWithdrawal(
            @RequestHeader("Authorization") String token,
            @RequestParam double amount,
            @RequestParam String bankName,
            @RequestParam String bankAccountNumber,
            @RequestParam String accountHolderName) {
        try {
            WithdrawalResponse response = withdrawalService.processWithdrawal(
                    token, amount, bankName, bankAccountNumber, accountHolderName);
            if ("failure".equals(response.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else {
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            WithdrawalResponse errorResponse = new WithdrawalResponse("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
