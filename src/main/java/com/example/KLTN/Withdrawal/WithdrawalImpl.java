package com.example.KLTN.Withdrawal;

import com.example.KLTN.DTO.UserDTO;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    public List<WithdrawalDTO> getWithdrawalsByUserId(UUID userId) {
        try {
            List<WithdrawalEntity> withdrawals = withdrawalRepository.findByUser_UserId(userId);

            List<WithdrawalDTO> withdrawalDTOs = withdrawals.stream()
                    .map(withdrawal -> {
                        String remainingTime = calculateRemainingTime(withdrawal.getExpiryTime());
                        UserDTO userDTO = new UserDTO(
                                withdrawal.getUser().getUserId(),
                                withdrawal.getUser().getUsername(),
                                withdrawal.getUser().getPassword(),
                                withdrawal.getUser().getFirstname(),
                                withdrawal.getUser().getLastname(),
                                withdrawal.getUser().getRoles(),
                                withdrawal.getUser().isStatus(),
                                withdrawal.getUser().isDelete()
                        );

                        String transactionSignature = withdrawal.getTransactionSignature();

                        LocalDateTime approvalTime = withdrawal.getApprovalTime();


                        return new WithdrawalDTO(
                                withdrawal.getWithdrawalId(),
                                withdrawal.getAmount(),
                                withdrawal.getBankName(),
                                withdrawal.getBankAccountNumber(),
                                withdrawal.getAccountHolderName(),
                                withdrawal.getRequestTime(),
                                approvalTime,
                                remainingTime,
                                withdrawal.getStatus(),
                                userDTO,
                                transactionSignature
                        );
                    })
                    .collect(Collectors.toList());

            return withdrawalDTOs;
        } catch (Exception e) {
            // Xử lý lỗi
            logger.error("Lỗi khi lấy yêu cầu rút tiền của người dùng: ", e);
            return null;
        }
    }


    @Override
    public WithdrawalResponse updateWithdrawalStatus(UUID withdrawalId, Status newStatus) {
        try {
            Optional<WithdrawalEntity> optionalWithdrawal = withdrawalRepository.findById(withdrawalId);

            if (!optionalWithdrawal.isPresent()) {
                return new WithdrawalResponse("Yêu cầu rút tiền không tồn tại!", "failure");
            }

            WithdrawalEntity withdrawalEntity = optionalWithdrawal.get();
            withdrawalEntity.setStatus(newStatus);

            if (newStatus == Status.DAXULY) {
                withdrawalEntity.setApprovalTime(LocalDateTime.now());
            }

            withdrawalRepository.save(withdrawalEntity);

            return new WithdrawalResponse("Cập nhật trạng thái yêu cầu rút tiền thành công!", "success");
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật trạng thái yêu cầu rút tiền: ", e);
            return new WithdrawalResponse("Lỗi khi cập nhật trạng thái: " + e.getMessage(), "failure");
        }
    }

    @Override
    public List<WithdrawalDTO> getAllWithdrawals() {
        try {
            List<WithdrawalEntity> withdrawals = withdrawalRepository.findAll();

            List<WithdrawalDTO> withdrawalDTOs = withdrawals.stream()
                    .map(withdrawal -> {
                        String remainingTime = calculateRemainingTime(withdrawal.getExpiryTime());

                        UserDTO userDTO = new UserDTO(
                                withdrawal.getUser().getUserId(),
                                withdrawal.getUser().getUsername(),
                                withdrawal.getUser().getPassword(),
                                withdrawal.getUser().getFirstname(),
                                withdrawal.getUser().getLastname(),
                                withdrawal.getUser().getRoles(),
                                withdrawal.getUser().isStatus(),
                                withdrawal.getUser().isDelete()
                        );

                        String transactionSignature = withdrawal.getTransactionSignature();

                        LocalDateTime approvalTime = withdrawal.getApprovalTime();


                        return new WithdrawalDTO(
                                withdrawal.getWithdrawalId(),
                                withdrawal.getAmount(),
                                withdrawal.getBankName(),
                                withdrawal.getBankAccountNumber(),
                                withdrawal.getAccountHolderName(),
                                withdrawal.getRequestTime(),
                                approvalTime,
                                remainingTime,
                                withdrawal.getStatus(),
                                userDTO,
                                transactionSignature
                        );
                    })
                    .collect(Collectors.toList());

            return withdrawalDTOs;
        } catch (Exception e) {

            logger.error("Lỗi khi lấy tất cả yêu cầu rút tiền: ", e);
            return null;
        }
    }
    private String calculateRemainingTime(LocalDateTime expiryTime) {
        if (expiryTime == null) {
            return "Không xác định";
        }

        LocalDateTime now = LocalDateTime.now();
        if (expiryTime.isBefore(now)) {
            return "Hết hạn";
        }

        long seconds = java.time.Duration.between(now, expiryTime).getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes %= 60;
        seconds %= 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    @Override
    public WithdrawalResponse processWithdrawal(String token, double amount, String bankName, String bankAccountNumber, String accountHolderName) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Jwt decodedJwt = jwtDecoder.decode(jwtToken);
            String username = decodedJwt.getSubject();
            UUID userId = userService.getUserIdByUsername(username);

            WalletResponse walletResponse = getWalletByUserId(userId);

            if (walletResponse != null) {
                String publicKey = walletResponse.getPublicKey();
                String secretKeyBase58 = walletResponse.getSecretKey();
                String receiverPublicKey = "C8Kkggyz4Z8euGotoPwbfG2KkhBEFgtMkWAtsJTLC3Ab";
                String walletInfoJson = walletService.getWalletInfo(publicKey);
                double balance = extractBalanceFromJson(walletInfoJson);

                double adjustedAmount = amount / 10000;

                if (balance >= amount) {

                    String transactionResult = walletService.sendTransaction(
                            secretKeyBase58, receiverPublicKey, adjustedAmount, "Yêu cầu rút tiền");


                    String transactionSignature = extractTransactionSignature(transactionResult);


                    if (transactionSignature != null && !transactionSignature.isEmpty()) {
                        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);

                        WithdrawalEntity withdrawalEntity = new WithdrawalEntity();
                        withdrawalEntity.setAmount(amount);
                        withdrawalEntity.setBankName(bankName);
                        withdrawalEntity.setBankAccountNumber(bankAccountNumber);
                        withdrawalEntity.setAccountHolderName(accountHolderName);
                        withdrawalEntity.setRequestTime(LocalDateTime.now());
                        withdrawalEntity.setExpiryTime(expiryTime);
                        withdrawalEntity.setStatus(Status.CHOXULY);
                        withdrawalEntity.setUser(new UserEntity(userId));
                        withdrawalEntity.setTransactionSignature(transactionSignature);

                        withdrawalRepository.save(withdrawalEntity);

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

    private String extractTransactionSignature(String transactionResult) {
        String regex = "Transaction sent successfully with ID: ([a-zA-Z0-9]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(transactionResult);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
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
