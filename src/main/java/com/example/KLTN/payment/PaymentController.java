package com.example.KLTN.payment;

import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Reponsitory.UserRepository;
import com.example.KLTN.Wallets.SolanaEntity;
import com.example.KLTN.Wallets.SolanaReponsitory;
import com.example.KLTN.Wallets.WalletService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class PaymentController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolanaReponsitory walletRepository;

    @Autowired
    private WalletService airdropService;

    @GetMapping("/payment_infor")
    public ResponseEntity<TransactionStatusDTO> transaction(HttpServletRequest request) {
        TransactionStatusDTO transactionStatusDTO = new TransactionStatusDTO();
        Map<String, String[]> parameterMap = request.getParameterMap();
        TransactionDetailsDTO transactionDetails = new TransactionDetailsDTO();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            String value = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]", "");
            switch (key) {
                case "vnp_TxnRef":
                    transactionDetails.setTxnRef(value);
                    break;
                case "vnp_Amount":
                    transactionDetails.setAmount(value);
                    break;
                case "vnp_BankCode":
                    transactionDetails.setBankCode(value);
                    break;
                case "vnp_BankTranNo":
                    transactionDetails.setBankTranNo(value);
                    break;
                case "vnp_CardType":
                    transactionDetails.setCardType(value);
                    break;
                case "vnp_OrderInfo":
                    transactionDetails.setOrderInfo(value);
                    String[] orderInfoParts = value.split(" by ");
                    if (orderInfoParts.length > 1) {
                        transactionDetails.setUsername(orderInfoParts[1]);
                    }
                    break;
                case "vnp_PayDate":
                    transactionDetails.setPayDate(value);
                    break;
                case "vnp_TransactionNo":
                    transactionDetails.setTransactionNo(value);
                    break;
                case "vnp_TransactionStatus":
                    transactionDetails.setTransactionStatus(value);
                    break;
                case "vnp_TmnCode":
                    transactionDetails.setTmnCode(value);
                    break;
                case "vnp_ResponseCode":
                    transactionDetails.setResponseCode(value);
                    break;
                default:
                    break;
            }
        }
        String responseCode = transactionDetails.getResponseCode();
        if ("00".equals(responseCode)) {
            transactionStatusDTO.setStatus("OK");
            transactionStatusDTO.setMessage("Giao dịch thành công.");
            String username = transactionDetails.getUsername();
            Optional<UserEntity> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                UserEntity user = userOptional.get();
                UUID userId = user.getUserId();
                Optional<SolanaEntity> walletOptional = walletRepository.findByUser_UserId(userId);
                if (walletOptional.isPresent()) {
                    SolanaEntity wallet = walletOptional.get();
                    String publicKey = wallet.getPublicKey();

                    try {
                        double amount = Double.parseDouble(transactionDetails.getAmount()) / 1000000;
                        String airdropResponse = airdropService.airdropFunds(publicKey, amount);
                        transactionStatusDTO.setAirdropResponse(airdropResponse);
                    } catch (Exception e) {
                        transactionStatusDTO.setMessage("Giao dịch thành công nhưng airdrop thất bại: " + e.getMessage());
                    }
                } else {
                    transactionStatusDTO.setMessage("Giao dịch thành công nhưng không tìm thấy ví của người dùng.");
                }
            } else {
                transactionStatusDTO.setMessage("Giao dịch thành công nhưng không tìm thấy người dùng.");
            }
        } else {
            transactionStatusDTO.setStatus("No");
            transactionStatusDTO.setMessage("Giao dịch thất bại.");
        }
        transactionStatusDTO.setData(transactionDetails);

        return ResponseEntity.ok(transactionStatusDTO);
    }


    @GetMapping("/pay")
    public String getPay(
            @RequestParam("amount") long amount,
            @RequestParam("username") String username,
            HttpServletRequest request) throws UnsupportedEncodingException {

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_OrderInfo = "Pay " + vnp_TxnRef + " by " + username;
        String vnp_IpAddr = Config.getIpAddress(request);
        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", "billpayment");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_Returnurl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;
    }
}