package com.example.KLTN.Wallets;

import com.example.KLTN.Configuration.WebSocketController;
import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Reponsitory.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
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
    @Autowired
    private SolanaReponsitory solanaRepository;

    private final RestTemplate restTemplate;
    public WalletService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public void checkAndNotifyChange(String publicKey) {
        String message = "Số dư hoặc lịch sử giao dịch của bạn đã thay đổi.";
        webSocketController.sendNotification(publicKey, message);
    }
    public SolanaEntity getWalletByUserId(UUID userId) {
        return walletRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId));
    }
    public String getTransactionsByProjectId(String projectId) {
        try {
            // Chạy script Node.js thông qua ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "node", "D:\\wallet_solana\\getTransactionsByProjectId.js", projectId
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Đọc kết quả từ Node.js script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                // Loại bỏ dòng cảnh báo "bigint: Failed to load bindings"
                if (!line.contains("bigint: Failed to load bindings")) {
                    output.append(line).append("\n");
                }
            }
            process.waitFor();

            return output.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error occurred while fetching transactions.";
        }
    }

    public String burnTokens(String senderSecretKeyBase58, String[] mintAddresses, String[] amounts,
                             String projectName, String projectId, String eventDescription,
                             String eventField, String eventReason, String evenContent) {
        try {
            StringBuilder args = new StringBuilder();
            args.append(String.join(",", mintAddresses != null ? mintAddresses : new String[0]));
            args.append(" ");
            args.append(String.join(",", amounts != null ? amounts : new String[0]));
            args.append(" ");
            args.append(projectName != null ? projectName : "");
            args.append(" ");
            args.append(projectId != null ? projectId : "");
            args.append(" ");
            args.append(eventDescription != null ? eventDescription : "");
            args.append(" ");
            args.append(eventField != null ? eventField : "");
            args.append(" ");
            args.append(eventReason != null ? eventReason : "");
            args.append(" ");
            args.append(evenContent != null ? evenContent : "");
            logger.info("Prepared arguments for Node.js script: {}", args.toString());
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "node", "D:\\wallet_solana\\burnToken2.js",
                    senderSecretKeyBase58,
                    String.join(",", mintAddresses),
                    String.join(",", amounts),
                    projectName != null ? projectName : "",
                    projectId != null ? projectId : "",
                    eventDescription != null ? eventDescription : "",
                    eventField != null ? eventField : "",
                    eventReason != null ? eventReason : "",
                    evenContent != null ? evenContent : ""
            );

            logger.info("Executing Node.js script with command: {}", String.join(" ", processBuilder.command()));

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("bigint: Failed to load bindings")) {
                    output.append(line).append("\n");
                }
            }
            process.waitFor();

            String outputString = output.toString().trim();
            logger.info("Output from Node.js burnToken2 script: {}", outputString);

            JSONObject result = new JSONObject();

            if (outputString.contains("Transaction to log content successful: ")) {
                String transactionId = outputString.replace("Transaction to log content successful: ", "").split("\n")[0];
                result.put("transactionId", transactionId);
                result.put("message", "Transaction to log content successful");
                result.put("status", "success");
            } else {
                String errorMessage = outputString.split("\n")[0];
                result.put("status", "failure");
                result.put("message", "Error during token burning");
                result.put("error", errorMessage);
            }
            return result.toString();

        } catch (IOException | InterruptedException e) {
            logger.error("Error during token burning: ", e);

            JSONObject result = new JSONObject();
            result.put("message", "Error during token burning");
            result.put("status", "failure");
            result.put("error", e.getMessage());

            return result.toString();
        }
    }


    public String getTransactionHistory2(String tokenAddress) {
        try {
            String jsonInputString = String.format(
                    "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"getConfirmedSignaturesForAddress2\", \"params\": [\"%s\", {\"limit\": 100}]}",
                    tokenAddress
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


                checkAndNotifyChange(tokenAddress);

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

    private boolean isValidBase58(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return input.matches("^[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]+$");
    }

    public String getTokenAddress(String publicKey, String mintAddress) {
        try {
            System.out.println("Received publicKey: " + publicKey);
            System.out.println("Received mintAddress: " + mintAddress);

            if (!isValidBase58(publicKey)) {
                return "{\"error\":\"Invalid public key format\"}";
            }
            if (!isValidBase58(mintAddress)) {
                return "{\"error\":\"Invalid mint address format\"}";
            }
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "node",
                    "D:\\wallet_solana\\getTokenAddress.js",
                    publicKey,
                    mintAddress
            );

            String command = String.join(" ", processBuilder.command());
            System.out.println("Executing command: " + command);

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.contains("bigint: Failed to load bindings")) {
                    output.append(line).append("\n");
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Node.js script exited with code: " + exitCode);
                return "{\"error\":\"Script Node.js failed with exit code: " + exitCode + "\"}";
            }

            String outputString = output.toString().trim();
            System.out.println("Output from Node.js: " + outputString);

            return outputString;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("message", "Lỗi khi gọi script Node.js: " + e.getMessage());
            return jsonResponse.toString();
        }
    }



    public String getTokenBalance(String mintAddressBase58, String tokenAccountAddressBase58) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "node",
                    "D:\\wallet_solana\\tokenBalance.js",
                    mintAddressBase58,
                    tokenAccountAddressBase58
            );
            String command = String.join(" ", processBuilder.command());
            System.out.println("Executing command: " + command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.contains("bigint: Failed to load bindings")) {
                    output.append(line).append("\n");
                }
            }

            process.waitFor();

            JSONObject jsonResponse = new JSONObject();
            String balance = "0";

            String outputString = output.toString().trim();
            if (!outputString.isEmpty()) {
                String[] lines = outputString.split("\n");
                for (String outputLine : lines) {
                    if (outputLine.startsWith("Số dư token: ")) {
                        balance = outputLine.replace("Số dư token: ", "").trim();
                        break;
                    }
                }
            }

            jsonResponse.put("balance", balance);
            jsonResponse.put("message", "Lấy số dư thành công.");
            return jsonResponse.toString();

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("balance", "0");
            jsonResponse.put("message", "Lỗi khi gọi script Node.js: " + e.getMessage());
            return jsonResponse.toString();
        }
    }


    public String transferToken(String senderSecretKeyBase58, String toAddressBase58, String mintAddressBase58, int amount, double solAmount, String receiverSecretKeyBase58) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "node",
                    "D:\\wallet_solana\\token_transfer.js",
                    senderSecretKeyBase58,
                    toAddressBase58,
                    mintAddressBase58,
                    String.valueOf(amount),
                    String.valueOf(solAmount),
                    receiverSecretKeyBase58
            );
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.contains("bigint: Failed to load bindings")) {
                    output.append(line).append("\n");
                }
            }

            process.waitFor();

            String outputString = output.toString().trim();
            logger.info("Output từ Node.js script: {}", outputString);

            return outputString;

        } catch (Exception e) {
            logger.error("Lỗi khi chuyển token: ", e);
            return "Lỗi khi chuyển token: " + e.getMessage();
        }
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

    public TokenCreationResponse createToken(String senderSecretKeyBase58, int tokenCount) {
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
            String tokenAddress = null;
            String mintToken = null;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");

                if (line.contains("Mint token đã tạo: ")) {
                    mintToken = line.substring(line.indexOf(": ") + 2).trim();
                }
                if (line.contains("Tạo tài khoản token: ")) {
                    tokenAddress = line.substring(line.indexOf(": ") + 2).trim();
                }
            }
            process.waitFor();

            String outputString = output.toString().trim();
            logger.info("Output from Node.js script: {}", outputString);

            if (mintToken == null || tokenAddress == null) {
                throw new Exception("Không thể lấy mint token hoặc địa chỉ token từ đầu ra.");
            }

            return new TokenCreationResponse(mintToken, tokenAddress);

        } catch (Exception e) {
            logger.error("Error creating token: ", e);
            return new TokenCreationResponse(null, "Error creating token: " + e.getMessage());
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
                    String tokenAddress = result.getJSONObject(i).getString("pubkey");
                    JSONObject tokenAmount = accountInfo.getJSONObject("tokenAmount");
                    long amount = tokenAmount.getLong("amount");

                    JSONObject tokenDetails = new JSONObject();
                    tokenDetails.put("mint", mint);
                    tokenDetails.put("tokenAddress", tokenAddress);
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
    public String deleteTransaction(String publicKey, String signature) {
        try {
            // Lấy danh sách giao dịch từ Solana RPC
            String transactionHistory = getTransactionHistory(publicKey);
            JSONObject jsonResponse = new JSONObject(transactionHistory);
            JSONArray transactionsArray = jsonResponse.getJSONArray("transactions");

            // Lọc các giao dịch không phải là giao dịch cần xóa
            JSONArray updatedTransactionsArray = new JSONArray();
            for (int i = 0; i < transactionsArray.length(); i++) {
                JSONObject transaction = transactionsArray.getJSONObject(i);
                if (!transaction.getString("signature").equals(signature)) {
                    updatedTransactionsArray.put(transaction);
                }
            }

            // Cập nhật danh sách giao dịch sau khi xóa
            jsonResponse.put("transactions", updatedTransactionsArray);

            return jsonResponse.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Error deleting transaction: " + e.getMessage() + "\"}";
        }
    }

    public String getTransactionHistory(String publicKey) {
        try {
            String jsonInputString = String.format(
                    "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"getConfirmedSignaturesForAddress2\", \"params\": [\"%s\", {\"limit\": 100   }]}",
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