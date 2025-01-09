package com.example.KLTN.Wallets;

import com.example.KLTN.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;


import java.util.*;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;
    private final JwtDecoder jwtDecoder;

    @Autowired
    public WalletController(WalletService walletService, UserService userService, JwtDecoder jwtDecoder) {
        this.walletService = walletService;
        this.userService = userService;
        this.jwtDecoder = jwtDecoder;
    }
    @GetMapping("/byProjectId")
    public ResponseEntity<String> getTransactionsByProjectId(@RequestParam String projectId) {
        String transactions = walletService.getTransactionsByProjectId(projectId);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/burn")
    public String burnTokens(
            @RequestParam String senderSecretKeyBase58,
            @RequestParam String[] mintAddresses,
            @RequestParam String[] amounts,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String eventDescription,
            @RequestParam(required = false) String eventField,
            @RequestParam(required = false) String eventReason,
            @RequestParam(required = false) String evenContent) {

        return walletService.burnTokens(senderSecretKeyBase58, mintAddresses, amounts,
                projectName, projectId, eventDescription,
                eventField, eventReason, evenContent);
    }
    @GetMapping("/address")
    public ResponseEntity<Map<String, String>> getTokenAddress(
            @RequestParam String publicKey,
            @RequestParam String mintAddress) {

        publicKey = publicKey.split(",")[0];
        mintAddress = mintAddress.split(",")[0];

        String tokenAddress = walletService.getTokenAddress(publicKey, mintAddress);

        Map<String, String> response = new HashMap<>();
        response.put("tokenAddress", tokenAddress);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/transaction-historyAdressToken")
    public ResponseEntity<String> getTransactionHistory(@RequestParam String tokenAddress) {
        try {
            String transactionHistory = walletService.getTransactionHistory2(tokenAddress);
            return ResponseEntity.ok(transactionHistory);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"Lỗi khi lấy lịch sử giao dịch: " + e.getMessage() + "\"}");
        }
    }
    @GetMapping("balance")
    public String checkTokenBalance(
            @RequestParam String mintAddress,
            @RequestParam String tokenAccountAddress) {
        return walletService.getTokenBalance(mintAddress, tokenAccountAddress);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> getWalletByUserId(@PathVariable UUID userId) {
        SolanaEntity solanaEntity = walletService.getWalletByUserId(userId);

        if (solanaEntity == null) {
            return ResponseEntity.notFound().build();
        }

        WalletResponse walletResponse = new WalletResponse();
        walletResponse.setPublicKey(solanaEntity.getPublicKey());
        walletResponse.setSecretKey(solanaEntity.getSecretKey());

        return ResponseEntity.ok(walletResponse);
    }
    @PostMapping("/transferToken")
    public ResponseEntity<Map<String, Object>> transferToken(
            @RequestParam String senderSecretKeyBase58,
            @RequestParam String toAddressBase58,
            @RequestParam String mintAddressBase58,
            @RequestParam int amount,
            @RequestParam double solAmount,
            @RequestParam String receiverSecretKeyBase58) {

        String result = walletService.transferToken(senderSecretKeyBase58, toAddressBase58, mintAddressBase58, amount, solAmount, receiverSecretKeyBase58);

        Map<String, Object> response = new HashMap<>();

        if (result.contains("Giao dịch đã được xác nhận với chữ ký:")) {
            String signature = result.split(": ")[1].split("\n")[0];
            response.put("success", true);
            response.put("message", "Giao dịch đã được thực hiện thành công");
            response.put("signature", signature);

        } else if (result.contains("Giao dịch chuyển token đã được xác nhận với chữ ký:") ||
                result.contains("Giao dịch chuyển SOL đã được xác nhận với chữ ký:")) {
            response.put("success", true);
            response.put("message", "Giao dịch đã được thực hiện thành công");

            String signature = result.split(": ")[1].split("\n")[0];
            response.put("signature", signature);

        } else {
            response.put("success", false);
            response.put("message", "Giao dịch thất bại: " + result);
        }

        return ResponseEntity.ok(response);
    }


    @GetMapping("/username/{publicKey}")
    public ResponseEntity<String> getUsername(@PathVariable String publicKey) {
        String username = walletService.getUsernameFromPublicKey(publicKey);
        if (username.startsWith("User not found") || username.startsWith("Wallet not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(username);
        }

        return ResponseEntity.ok(username);
    }


    @PostMapping("/createToken")
    public ResponseEntity<Map<String, String>> createToken(@RequestParam String senderSecretKeyBase58,
                                                           @RequestParam int tokenCount) {
        try {
            TokenCreationResponse tokenResponse = walletService.createToken(senderSecretKeyBase58, tokenCount);

            Map<String, String> response = new HashMap<>();
            response.put("mintToken", tokenResponse.getMintToken());
            response.put("tokenAddress", tokenResponse.getTokenAddress());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Lỗi khi tạo token: " + e.getMessage()));
        }
    }


    @PostMapping("/create")
    public String createWallet(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");

            Jwt decodedJwt = jwtDecoder.decode(jwtToken);
            String username = decodedJwt.getSubject();

            UUID userId = userService.getUserIdByUsername(username);

            return walletService.createWallet(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing token: " + e.getMessage();
        }
    }
    @GetMapping("/info")
    public ResponseEntity<?> getWalletInfo(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Jwt decodedJwt = jwtDecoder.decode(jwtToken);
            String username = decodedJwt.getSubject();
            UUID userId = userService.getUserIdByUsername(username);
            Optional<SolanaEntity> walletOptional = walletService.findWalletByUserId(userId);

            if (walletOptional.isPresent()) {
                String publicKey = walletOptional.get().getPublicKey();
                String walletInfoJson = walletService.getWalletInfo(publicKey);

                return ResponseEntity.ok(walletInfoJson);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Public key not found for user: " + username);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting wallet info: " + e.getMessage());
        }
    }

    @GetMapping("/secretKey")
    public ResponseEntity<String> getWalletSecretByOwner(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Jwt decodedJwt = jwtDecoder.decode(jwtToken);
            String username = decodedJwt.getSubject();

            UUID userId = userService.getUserIdByUsername(username);

            Optional<SolanaEntity> walletOptional = walletService.findWalletByUserId(userId);

            if (walletOptional.isPresent()) {
                String secretKey = walletOptional.get().getSecretKey();
                return ResponseEntity.ok(secretKey);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Wallet not found for user: " + username);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting wallet info: " + e.getMessage());
        }
    }

    @GetMapping("/token")
    public ResponseEntity<String> getTokenAccountsByOwner(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Jwt decodedJwt = jwtDecoder.decode(jwtToken);
            String username = decodedJwt.getSubject();

            UUID userId = userService.getUserIdByUsername(username);

            Optional<SolanaEntity> walletOptional = walletService.findWalletByUserId(userId);

            if (walletOptional.isPresent()) {
                String publicKey = walletOptional.get().getPublicKey();
                String transations = walletService.getTokenAccountsByOwner(publicKey);
                return ResponseEntity.ok(transations);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Public key not found for user: " + username);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting wallet info: " + e.getMessage());
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Jwt decodedJwt = jwtDecoder.decode(jwtToken);
            String username = decodedJwt.getSubject();

            UUID userId = userService.getUserIdByUsername(username);

            Optional<SolanaEntity> walletOptional = walletService.findWalletByUserId(userId);

            if (walletOptional.isPresent()) {
                String publicKey = walletOptional.get().getPublicKey();
                String transations = walletService.getTransactionHistory(publicKey);
                return ResponseEntity.ok(transations);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Public key not found for user: " + username);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting wallet info: " + e.getMessage());
        }
    }
    @DeleteMapping("/transactions/{signature}")
    public ResponseEntity<?> deleteTransaction(
            @RequestHeader("Authorization") String token,
            @PathVariable("signature") String signature) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Jwt decodedJwt = jwtDecoder.decode(jwtToken);
            String username = decodedJwt.getSubject();

            UUID userId = userService.getUserIdByUsername(username);

            Optional<SolanaEntity> walletOptional = walletService.findWalletByUserId(userId);

            if (walletOptional.isPresent()) {
                String publicKey = walletOptional.get().getPublicKey();
                String updatedTransactions = walletService.deleteTransaction(publicKey, signature);
                return ResponseEntity.ok(updatedTransactions);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Public key not found for user: " + username);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting transaction: " + e.getMessage());
        }
    }

    @PostMapping("/airdrop")
    public ResponseEntity<String> airdrop(@RequestParam String recipientPubkey, @RequestParam double amount) {
        try {
            String transactionSignature = walletService.airdropFunds(recipientPubkey, amount);
            return ResponseEntity.ok(transactionSignature);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Airdrop failed: " + e.getMessage());
        }
    }
    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> sendTransaction(
            @RequestParam String senderSecretKeyBase58,
            @RequestParam String receiverPublicKey,
            @RequestParam double amount,
            @RequestParam(required = false) String content) {

        Map<String, String> response = new HashMap<>();

        try {
            String result = walletService.sendTransaction(senderSecretKeyBase58, receiverPublicKey, amount, content);

            response.put("message", "Transaction successful");
            response.put("transactionId", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Transaction failed");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}
