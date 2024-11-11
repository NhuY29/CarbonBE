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
    public SolanaEntity getWalletByUserId(UUID userId) {
        return walletRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId));
    }
    public String getTransactionHistory2(String tokenAddress) {
        try {
            String jsonInputString = String.format(
                    "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"getConfirmedSignaturesForAddress2\", \"params\": [\"%s\", {\"limit\": 10   }]}",
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


    public String getTokenBalance(String mintAddressBase58, String tokenAccountAddressBase58) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "node",
                    "D:\\wallet_solana\\tokenBalance.js",
                    mintAddressBase58,
                    tokenAccountAddressBase58
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                // Lọc bỏ thông báo không cần thiết
                if (!line.contains("bigint: Failed to load bindings")) {
                    output.append(line).append("\n");
                }
            }

            process.waitFor();

            // Tạo JSON response
            JSONObject jsonResponse = new JSONObject();
            String balance = "0"; // Khởi tạo số dư mặc định

            // Phân tích đầu ra để lấy số dư
            String outputString = output.toString().trim();
            if (!outputString.isEmpty()) {
                // Giả định đầu ra có định dạng như sau: "Số dư token: 50000000000"
                String[] lines = outputString.split("\n");
                for (String outputLine : lines) {
                    if (outputLine.startsWith("Số dư token: ")) {
                        balance = outputLine.replace("Số dư token: ", "").trim();
                        break; // Kết thúc vòng lặp khi đã tìm thấy số dư
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

            // Đọc từng dòng đầu ra và lọc bỏ dòng không mong muốn
            while ((line = reader.readLine()) != null) {
                if (!line.contains("bigint: Failed to load bindings")) { // Lọc bỏ dòng thông báo bigint
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
            String tokenAddress = null; // Biến để lưu địa chỉ token
            String mintToken = null; // Biến để lưu mint token

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                // Kiểm tra xem dòng có chứa thông tin mint token không
                if (line.contains("Mint token đã tạo: ")) {
                    mintToken = line.substring(line.indexOf(": ") + 2).trim(); // Lấy mint token
                }
                // Kiểm tra xem dòng có chứa thông tin địa chỉ tài khoản token không
                if (line.contains("Tạo tài khoản token: ")) {
                    tokenAddress = line.substring(line.indexOf(": ") + 2).trim(); // Lấy địa chỉ token
                }
            }
            process.waitFor();

            String outputString = output.toString().trim();
            logger.info("Output from Node.js script: {}", outputString);

            if (mintToken == null || tokenAddress == null) {
                throw new Exception("Không thể lấy mint token hoặc địa chỉ token từ đầu ra.");
            }

            // Trả về đối tượng chứa mintToken và tokenAddress
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
                    String tokenAddress = result.getJSONObject(i).getString("pubkey"); // Lấy địa chỉ token (token address)
                    JSONObject tokenAmount = accountInfo.getJSONObject("tokenAmount");
                    long amount = tokenAmount.getLong("amount");

                    JSONObject tokenDetails = new JSONObject();
                    tokenDetails.put("mint", mint);
                    tokenDetails.put("tokenAddress", tokenAddress); // Thêm token address
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
                    "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"getConfirmedSignaturesForAddress2\", \"params\": [\"%s\", {\"limit\": 10   }]}",
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