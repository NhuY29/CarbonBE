package com.example.KLTN.Wallets;

import com.example.KLTN.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public ResponseEntity<String> sendTransaction(
            @RequestParam String senderSecretKeyBase58,
            @RequestParam String receiverPublicKey,
            @RequestParam double amount,
            @RequestParam(required = false) String content) {

        try {
            String result = walletService.sendTransaction(senderSecretKeyBase58, receiverPublicKey, amount, content);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Transaction failed: " + e.getMessage());
        }
    }

}
