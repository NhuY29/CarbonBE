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
    @GetMapping("/username/{publicKey}")
    public ResponseEntity<String> getUsername(@PathVariable String publicKey) {
        String username = walletService.getUsernameFromPublicKey(publicKey);
        if (username.startsWith("User not found") || username.startsWith("Wallet not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(username);
        }

        return ResponseEntity.ok(username);
    }


    @PostMapping("/createToken")
    public String createToken(@RequestParam String senderSecretKeyBase58,
                              @RequestParam int tokenCount) {
        return walletService.createToken(senderSecretKeyBase58, tokenCount);
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
