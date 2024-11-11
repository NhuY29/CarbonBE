package com.example.KLTN.Withdrawal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/withdrawal")
public class WithdrawalController {
    @Autowired
    private WithdrawalService withdrawalService;

    @PostMapping("/request")
    public ResponseEntity<WithdrawalResponse> requestWithdrawal(
            @RequestHeader("Authorization") String token,
            @RequestParam double amount,
            @RequestParam String bankName,
            @RequestParam String bankAccountNumber,
            @RequestParam String accountHolderName) {
        try {
            // Xử lý yêu cầu rút tiền từ dịch vụ
            WithdrawalResponse response = withdrawalService.processWithdrawal(
                    token, amount, bankName, bankAccountNumber, accountHolderName);

            // Kiểm tra xem phản hồi có chứa trạng thái thất bại hay không
            if ("failure".equals(response.getStatus())) {
                // Nếu thất bại, trả về HTTP 400 (BAD_REQUEST)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else {
                // Nếu thành công, trả về HTTP 200 (OK)
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            // Xử lý lỗi hệ thống
            WithdrawalResponse errorResponse = new WithdrawalResponse("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
