package com.example.KLTN.Wallets;

import com.example.KLTN.Configuration.WebSocketController;
import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Reponsitory.UserRepository;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Transaction;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.web.client.RestTemplate;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    @Autowired
    private SolanaReponsitory walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private WebSocketController webSocketController;

    private final RestTemplate restTemplate;
    public WalletService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void checkAndNotifyChange(String publicKey) {
        String message = "Số dư hoặc lịch sử giao dịch của bạn đã thay đổi.";
        webSocketController.sendNotification(publicKey, message);
    }

    public String getUsernameFromPublicKey(String publicKey) {
        Optional<SolanaEntity> walletOptional = walletRepository.findByPublicKey(publicKey);
        if (walletOptional.isPresent()) {
            SolanaEntity wallet = walletOptional.get();
            UUID userId = wallet.getUser().getUserId();
            Optional<UserEntity> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                UserEntity user = userOptional.get();
                return user.getUsername();
            } else {
                return "User not found for userId: " + userId;
            }
        } else {
            return "Wallet not found for publicKey: " + publicKey;
        }
    }

    public String createToken(String senderSecretKeyBase58, int tokenCount) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "node",
                    "D:\\wallet_solana\\createToken.js",
                    senderSecretKeyBase58,
                    String.valueOf(tokenCount)
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();

            String outputString = output.toString().trim();
            logger.info("Output from Node.js script: {}", outputString);

            String mintToken = extractMintToken(outputString);
            return mintToken;

        } catch (Exception e) {
            logger.error("Error creating token: ", e);
            return "Error creating token: " + e.getMessage();
        }
    }

    private String extractMintToken(String output) {
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.startsWith("Mint token đã tạo: ")) {
                return line.substring("Mint token đã tạo: ".length()).trim();
            }
        }
        return "Không tìm thấy mint token.";
    }


    public String getTokenAccountsByOwner(String publicKey) {
        try {
            String jsonInputString = String.format(
                    "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"getTokenAccountsByOwner\", \"params\": [\"%s\", {\"programId\": \"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA\"}, {\"encoding\": \"jsonParsed\"}]}",
                    publicKey
            );

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8899"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray result = jsonResponse.getJSONObject("result").getJSONArray("value");

                JSONArray tokensArray = new JSONArray();

                for (int i = 0; i < result.length(); i++) {
                    JSONObject accountInfo = result.getJSONObject(i).getJSONObject("account").getJSONObject("data").getJSONObject("parsed").getJSONObject("info");
                    String mint = accountInfo.getString("mint");
                    JSONObject tokenAmount = accountInfo.getJSONObject("tokenAmount");
                    long amount = tokenAmount.getLong("amount");

                    JSONObject tokenDetails = new JSONObject();
                    tokenDetails.put("mint", mint);
                    tokenDetails.put("amount", amount);

                    tokensArray.put(tokenDetails);
                }

                JSONObject resultJson = new JSONObject();
                resultJson.put("tokens", tokensArray);

                return resultJson.toString();
            } else {
                return String.format("{\"error\": \"Error from Solana RPC: HTTP %d - %s\"}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Error getting token accounts: " + e.getMessage() + "\"}";
        }
    }

    public String createWallet(UUID userId) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("node", "D:\\wallet_solana\\createWallet.js");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();

            String outputString = output.toString().trim();
            logger.info("Output from Node.js script: {}", outputString);

            String[] lines = outputString.split("\n");
            String publicKey = null;
            String secretKeyBase58 = null;

            for (String lineText : lines) {
                if (lineText.startsWith("Public Key:")) {
                    publicKey = lineText.split(":")[1].trim();
                } else if (lineText.startsWith("Secret Key (Base58):")) {
                    secretKeyBase58 = lineText.split(":")[1].trim();
                }
            }

            if (publicKey != null && secretKeyBase58 != null) {
                Optional<UserEntity> userOptional = userRepository.findById(userId);
                if (userOptional.isEmpty()) {
                    logger.error("User not found with ID: {}", userId);
                    return "User not found";
                }
                UserEntity user = userOptional.get();

                SolanaEntity wallet = new SolanaEntity();
                wallet.setPublicKey(publicKey);
                wallet.setSecretKey(secretKeyBase58);
                wallet.setUser(user);
                walletRepository.save(wallet);

                return "Wallet created and saved successfully";
            } else {
                logger.error("Error: Missing publicKey or secretKeyBase58 in output.");
                return "Error: Missing publicKey or secretKeyBase58 in output.";
            }
        } catch (Exception e) {
            logger.error("Error creating wallet: ", e);
            return "Error creating wallet: " + e.getMessage();
        }
    }

    public Optional<SolanaEntity> findWalletByUserId(UUID userId) {
        Optional<UserEntity> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            return walletRepository.findByUser(user);
        }
        return Optional.empty();
    }

    public String getWalletInfo(String publicKey) {
        try {
            String jsonInputString = String.format(
                    "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"getBalance\", \"params\": [\"%s\"]}",
                    publicKey
            );

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8899"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONObject result = jsonResponse.getJSONObject("result");
                long balance = result.getLong("value");
                String address = publicKey;

                JSONObject resultJson = new JSONObject();
                resultJson.put("address", address);
                resultJson.put("balance", balance / 1_000_00);

                // Notify balance change
                checkAndNotifyChange(publicKey);

                return resultJson.toString();
            } else {
                return String.format("{\"error\": \"Error from Solana RPC: HTTP %d - %s\"}",
                        response.statusCode(), response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Error getting wallet info: " + e.getMessage() + "\"}";
        }
    }

    public String getTransactionHistory(String publicKey) {
        try {
            String jsonInputString = String.format(
                    "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"getConfirmedSignaturesForAddress2\", \"params\": [\"%s\", {\"limit\": 10}]}",
                    publicKey
            );

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8899"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray result = jsonResponse.getJSONArray("result");
                JSONArray transactionsArray = new JSONArray();

                for (int i = 0; i < result.length(); i++) {
                    JSONObject transaction = result.getJSONObject(i);
                    String signature = transaction.getString("signature");
                    long slot = transaction.getLong("slot");
                    long blockTime = transaction.optLong("blockTime", 0);


                    String resultStatus = transaction.opt("err") != null ? "Failed" : "Success";


                    String timestamp = blockTime > 0 ? new java.util.Date(blockTime * 1000).toString() : "Unknown time";
                    String age = calculateAge(blockTime);


                    JSONObject transactionDetails = new JSONObject();
                    transactionDetails.put("signature", signature);
                    transactionDetails.put("block", slot);
                    transactionDetails.put("age", age);
                    transactionDetails.put("timestamp", timestamp);
                    transactionDetails.put("result", resultStatus);

                    transactionsArray.put(transactionDetails);
                }

                JSONObject resultJson = new JSONObject();
                resultJson.put("transactions", transactionsArray);


                checkAndNotifyChange(publicKey);

                return resultJson.toString();
            } else {
                return String.format("{\"error\": \"Error from Solana RPC: HTTP %d - %s\"}",
                        response.statusCode(), response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Error getting transaction history: " + e.getMessage() + "\"}";
        }
    }


    private String calculateAge(long blockTime) {
        if (blockTime <= 0) {
            return "Unknown age";
        }

        long currentTime = System.currentTimeMillis() / 1000;
        long ageInSeconds = currentTime - blockTime;


        if (ageInSeconds < 60) {
            return ageInSeconds + " seconds ago";
        } else if (ageInSeconds < 3600) {
            return (ageInSeconds / 60) + " minutes ago";
        } else if (ageInSeconds < 86400) {
            return (ageInSeconds / 3600) + " hours ago";
        } else {
            return (ageInSeconds / 86400) + " days ago";
        }
    }

    public String airdropFunds(String recipientPubkey, double amount) throws Exception {
        long lamports = (long) (amount * 1_000_000_000);

        String jsonInputString = String.format(
                "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"requestAirdrop\", \"params\": [\"%s\", %d]}",
                recipientPubkey, lamports
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8899"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String responseBody = response.body();
            System.out.println("Response from Solana API: " + responseBody);

            // Notify balance change
            checkAndNotifyChange(recipientPubkey);

            return responseBody;
        } else {
            throw new Exception("Airdrop failed with HTTP status code " + response.statusCode() + ": " + response.body());
        }
    }

    public String sendTransaction(String senderSecretKeyBase58, String receiverPublicKey, double amount, String content) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "node",
                    "D:\\wallet_solana\\sendTransaction.js",
                    senderSecretKeyBase58,
                    receiverPublicKey,
                    String.valueOf(amount),
                    content != null ? content : ""
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();

            String outputString = output.toString().trim();
            logger.info("Output from Node.js script: {}", outputString);

            checkAndNotifyChange(receiverPublicKey);

            return outputString;

        } catch (Exception e) {
            logger.error("Error during transaction: ", e);
            return "Error during transaction: " + e.getMessage();
        }
    }
}