package com.example.KLTN.Service;

import com.example.KLTN.DTO.IntrospectDTO;
import com.example.KLTN.DTO.ResponseDTO;
import com.example.KLTN.Entity.InvalidatedToken;
import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Reponsitory.AuthenticationRepositories;
import com.example.KLTN.Reponsitory.InvalidatedReponsitory;
import com.example.KLTN.Request.AuthenticationRequest;
import com.example.KLTN.Request.IntrospectRequest;
import com.example.KLTN.Request.logoutRequest;
import com.example.KLTN.Wallets.SolanaEntity;
import com.example.KLTN.Wallets.SolanaReponsitory;
import com.example.KLTN.Wallets.WalletService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

@Component
public class AuthenticationImpl implements AuthenticationService {
    @Autowired
    AuthenticationRepositories authenticationRepositories;
    @Autowired
    InvalidatedReponsitory invalidatedReponsitory;
    @Autowired
    WalletService walletService;
    @Autowired
    SolanaReponsitory walletRepository;
    @Override
    public ResponseDTO authenticate(AuthenticationRequest request) {
        Optional<UserEntity> userOptional = authenticationRepositories.findByUsername(request.getUsername());
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();

            if (!user.isStatus()) {
                return new ResponseDTO(false, "Tài khoản không hợp lệ hoặc chờ phê duyệt!");
            }


            if (user.isDelete()) {
                return new ResponseDTO(false, "Tài khoản đã bị xóa!");
            }

            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                try {
                    Optional<SolanaEntity> walletOptional = walletRepository.findByUser(user);

                    if (walletOptional.isEmpty()) {
                        String createWalletResult = walletService.createWallet(user.getUserId());

                        if (createWalletResult.startsWith("Error")) {
                            return new ResponseDTO(false, "Lỗi khi tạo ví: " + createWalletResult);
                        }
                    }
                    var token = generateToken(user);
                    return new ResponseDTO(true, token);
                } catch (JOSEException e) {
                    e.printStackTrace();
                    return new ResponseDTO(false, "Đã xảy ra lỗi khi tạo token!");
                }
            } else {
                return new ResponseDTO(false, "UserName hoặc Password không đúng!");
            }
        } else {
            return new ResponseDTO(false, "UserName hoặc Password không đúng!");
        }
    }



    @NonFinal
    protected static final String Singer_key = "sCj1CBV+VSn6qlZMQxQ0eSEpFsey7Zqp2gaVmLR/3LgOc0UTappt5pCypZt/PWsa";

    String generateToken(UserEntity user) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("yyy.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope",buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        jwsObject.sign(new MACSigner(Singer_key.getBytes()));
        return jwsObject.serialize();
    }
    private String buildScope(UserEntity user) {
        StringJoiner scope = new StringJoiner(" ");
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            String[] rolesArray = user.getRoles().split(",");
            for (String role : rolesArray) {
                scope.add(role.trim());
            }
        }
        return scope.toString();
    }
    private SignedJWT verify(String token) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(Singer_key);
        SignedJWT jwt = SignedJWT.parse(token);
        Date expirationTime = jwt.getJWTClaimsSet().getExpirationTime();
        var verify = jwt.verify(verifier);
        if(!verify && expirationTime.after(new Date()))
            throw new RuntimeException("Unauthenticated");
        if(invalidatedReponsitory.existsById(jwt.getJWTClaimsSet().getJWTID()))
            throw new RuntimeException("Unauthenticated");
        return jwt;
    }
    public ResponseDTO logout(logoutRequest request) throws ParseException, JOSEException {
        var signToken = verify(request.getToken());
        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryDate(expiryTime)
                .build();
        invalidatedReponsitory.save(invalidatedToken);
        return new ResponseDTO(true, "Đăng xuất thành công!");
    }
    public IntrospectDTO introspect (IntrospectRequest introspectRequest) throws ParseException, JOSEException{
        var token = introspectRequest.getToken();
        boolean isValid = true;
        try{
            verify(token);
        }catch (Exception e){
            isValid = false;
        }
        return IntrospectDTO.builder()
                .valid(isValid)
                .build();
    }
}
