package com.example.KLTN.Withdrawal;

import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Enum.Status;
import com.example.KLTN.Service.UserService;
import com.example.KLTN.Wallets.SolanaEntity;
import com.example.KLTN.Wallets.WalletResponse;
import com.example.KLTN.Wallets.WalletService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class WithdrawalImpl implements WithdrawalService {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawalImpl.class);

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    @Override
    public WithdrawalResponse processWithdrawal(String token, double amount, String bankName, String bankAccountNumber, String accountHolderName) {
        try {
            // Xử lý và giải mã token
            String jwtToken = token.replace("Bearer ", "");
            Jwt decodedJwt = jwtDecoder.decode(jwtToken);
            String username = decodedJwt.getSubject();
            UUID userId = userService.getUserIdByUsername(username);

            // Lấy ví của người dùng
            WalletResponse walletResponse = getWalletByUserId(userId);

            if (walletResponse != null) {
                String publicKey = walletResponse.getPublicKey();
                String secretKeyBase58 = walletResponse.getSecretKey();
                String receiverPublicKey = "AGCCPPmXodzWWzGrH2F28byWwW8bGKiWAUTYCeyvyiSp";
                String walletInfoJson = walletService.getWalletInfo(publicKey);
                double balance = extractBalanceFromJson(walletInfoJson);

                // Chia số tiền rút cho 10000 trước khi thực hiện giao dịch
                double adjustedAmount = amount / 10000;

                if (balance >= amount) {
                    // Gửi giao dịch chuyển tiền với số tiền đã điều chỉnh
                    String transactionResult = walletService.sendTransaction(
                            secretKeyBase58, receiverPublicKey, adjustedAmount, "Yêu cầu rút tiền");

                    // Kiểm tra kết quả giao dịch
                    if (transactionResult != null) {
                        // Nếu giao dịch thành công, lưu yêu cầu rút tiền vào cơ sở dữ liệu
                        WithdrawalEntity withdrawalEntity = new WithdrawalEntity();
                        withdrawalEntity.setAmount(amount);  // Lưu lại số tiền ban đầu, chưa chia
                        withdrawalEntity.setBankName(bankName);
                        withdrawalEntity.setBankAccountNumber(bankAccountNumber);
                        withdrawalEntity.setAccountHolderName(accountHolderName);
                        withdrawalEntity.setRequestTime(LocalDateTime.now());
                        withdrawalEntity.setStatus(Status.DAYEUCAU);
                        withdrawalEntity.setUser(new UserEntity(userId));

                        withdrawalRepository.save(withdrawalEntity);

                        logger.info("Transaction successful for withdrawal request: {}, transactionId: {}", withdrawalEntity.getWithdrawalId(), transactionResult);
                        return new WithdrawalResponse("Yêu cầu rút tiền đã được tạo thành công và chuyển tiền hoàn tất!", "success");
                    } else {
                        return new WithdrawalResponse("Giao dịch chuyển tiền không thành công!", "failure");
                    }
                } else {
                    return new WithdrawalResponse("Số dư không đủ để rút tiền!", "failure");
                }
            } else {
                return new WithdrawalResponse("Không tìm thấy ví của người dùng.", "failure");
            }
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý rút tiền: ", e);
            return new WithdrawalResponse("Lỗi khi xử lý rút tiền: " + e.getMessage(), "failure");
        }
    }



    private WalletResponse getWalletByUserId(UUID userId) {
        SolanaEntity solanaEntity = walletService.getWalletByUserId(userId);

        if (solanaEntity == null) {
            logger.warn("Không tìm thấy ví cho userId: {}", userId);
            return null;
        }

        WalletResponse walletResponse = new WalletResponse();
        walletResponse.setPublicKey(solanaEntity.getPublicKey());
        walletResponse.setSecretKey(solanaEntity.getSecretKey());

        return walletResponse;
    }

    private double extractBalanceFromJson(String walletInfoJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(walletInfoJson);
            return jsonNode.get("balance").asDouble();
        } catch (Exception e) {
            logger.error("Lỗi khi trích xuất số dư từ JSON: ", e);
            return 0.0;
        }
    }
}
