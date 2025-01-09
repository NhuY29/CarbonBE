package com.example.KLTN.Service;
import com.example.KLTN.Buyer.BuyerDTO;
import com.example.KLTN.Buyer.BuyerService;
import com.example.KLTN.Configuration.CustomJWTDecoder;
import com.example.KLTN.DTO.MailDTO;
import com.example.KLTN.DTO.ResponseDTO;
import com.example.KLTN.DTO.UserDTO;
import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Reponsitory.UserRepository;
import com.example.KLTN.Seller.SellerDTO;
import com.example.KLTN.Seller.SellerService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UserImpl implements UserService {

    private static final Logger logger = Logger.getLogger(UserImpl.class.getName());
    @Autowired
    JavaMailSender mailSender;
    @Autowired
    SpringTemplateEngine templateEngine;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SellerService sellerService;
    @Autowired
    private BuyerService buyerService;
    @Autowired
    private CustomJWTDecoder customJWTDecoder;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Override
    public ResponseDTO createUser(UserDTO userDTO, BuyerDTO buyerDTO, SellerDTO sellerDTO) {
        try {
            if (userDTO.getUsername() == null || userDTO.getUsername().isEmpty()) {
                throw new IllegalArgumentException("Username không thể là null hoặc rỗng");
            }

            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(userDTO.getUsername());
            userEntity.setPassword(passwordEncoder.encode(Optional.ofNullable(userDTO.getPassword()).orElse("default_password")));
            userEntity.setFirstname(userDTO.getFirstname());
            userEntity.setLastname(userDTO.getLastname());
            userEntity.setRoles(userDTO.getRoles());
            userEntity.setStatus(false);
            userEntity = userRepository.save(userEntity);
            logger.info("User saved: " + userEntity.getUsername());

            if (userDTO.getRoles().contains("ROLE_ADMIN")) {
            } else if (userDTO.getRoles().contains("ROLE_BUYER")) {
                if (buyerDTO != null) {
                    buyerDTO.setUserId(userEntity.getUserId());
                    buyerDTO.setBuyerId(UUID.randomUUID());
                    buyerService.createBuyer(buyerDTO);
                } else {
                    throw new IllegalArgumentException("Thông tin BuyerDTO không được cung cấp cho ROLE_BUYER");
                }
            }

            else if (userDTO.getRoles().contains("ROLE_SELLER")) {
                if (sellerDTO != null) {
                    sellerDTO.setUserId(userEntity.getUserId());
                    sellerService.createSeller(sellerDTO);
                } else {
                    throw new IllegalArgumentException("Thông tin SellerDTO không được cung cấp cho ROLE_SELLER");
                }
            } else {
                throw new IllegalArgumentException("Vai trò không hợp lệ");
            }

            return new ResponseDTO(true, "Đăng ký thành công, vui lòng chờ phê duyệt.");
        } catch (Exception e) {
            logger.severe("Không thể đăng ký người dùng: " + e.getMessage());
            return new ResponseDTO(false, "Có lỗi xảy ra khi đăng ký người dùng.");
        }
    }
    public UUID getUserIdByUsername(String username) {
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            return userOptional.get().getUserId();
        }
        return null;
    }

    public Page<UserDTO> getAll(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        Page<UserEntity> userEntities = userRepository.findByIsDeletedFalseAndStatusTrue(pageable);

        List<UserDTO> userDTOs = userEntities.getContent().stream()
                .map(user -> new UserDTO(
                        user.getUserId(),
                        user.getUsername(),
                        user.getPassword(),
                        user.getFirstname(),
                        user.getLastname(),
                        user.getRoles(),
                        user.isStatus(),
                        user.isDelete()))
                .collect(Collectors.toList());

        return new PageImpl<>(userDTOs, pageable, userEntities.getTotalElements());
    }

    public Page<UserDTO> searchUsers(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<UserEntity> userEntities = userRepository.searchUsers(searchTerm, pageable);

        List<UserDTO> userDTOs = userEntities.getContent().stream()
                .map(user -> new UserDTO(
                        user.getUserId(),
                        user.getUsername(),
                        user.getPassword(),
                        user.getFirstname(),
                        user.getLastname(),
                        user.getRoles(),
                        user.isStatus(),
                        user.isDelete()))
                .collect(Collectors.toList());

        return new PageImpl<>(userDTOs, pageable, userEntities.getTotalElements());
    }
    public UserDTO getUserById(UUID userId) {
        Optional<UserEntity> userEntityOptional = userRepository.findById(userId);

        if (userEntityOptional.isPresent()) {
            UserEntity userEntity = userEntityOptional.get();

            // Chuyển đổi UserEntity sang UserDTO
            UserDTO userDTO = new UserDTO(
                    userEntity.getUserId(),
                    userEntity.getUsername(),
                    userEntity.getPassword(),
                    userEntity.getFirstname(),
                    userEntity.getLastname(),
                    userEntity.getRoles(),
                    userEntity.isStatus(),
                    userEntity.isDelete()
            );
            return userDTO;
        } else {
            return null; // Trả về null nếu không tìm thấy người dùng
        }
    }


    public ResponseDTO updateUserStatus(UUID userId, boolean status) {
        logger.info("Starting to update status for user with ID: " + userId);
        try {
            Optional<UserEntity> userEntityOptional = userRepository.findById(userId);
            if (userEntityOptional.isPresent()) {
                UserEntity userEntity = userEntityOptional.get();
                userEntity.setStatus(status);
                userRepository.save(userEntity);
                logger.info("Successfully updated status for user: " + userEntity.getUsername());
                return new ResponseDTO(true, "Cập nhật trạng thái thành công.");
            } else {
                logger.warning("User not found with ID: " + userId);
                return new ResponseDTO(false, "Người dùng không tồn tại.");
            }
        } catch (Exception e) {
            logger.severe("Lỗi khi cập nhật trạng thái người dùng với ID: " + userId + ". Exception: " + e.getMessage());
            return new ResponseDTO(false, "Không thể cập nhật trạng thái người dùng.");
        }
    }

    public String getUsernameById(UUID id) {
        return userRepository.findById(id)
                .map(UserEntity::getUsername)
                .orElse(null);
    }

    public void sendHtmlMail(MailDTO dataMail, String templateName) throws MessagingException {
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariable("props", dataMail.getProps());

        String html = templateEngine.process(templateName, context);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");
        messageHelper.setTo(dataMail.getTo());
        messageHelper.setSubject(dataMail.getSubject());
        messageHelper.setText(html, true);

        mailSender.send(mimeMessage);
    }

    @Override
    public List<UserDTO> getAllUsersTrue() {
        List<UserEntity> userEntities = userRepository.findAllByStatusTrueAndIsDeleteFalse();
        List<UserDTO> userDTOs = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(userEntity.getUsername());
            userDTO.setFirstname(userEntity.getFirstname());
            userDTO.setPassword(userEntity.getPassword());
            userDTO.setUserId(userEntity.getUserId());
            userDTO.setLastname(userEntity.getLastname());
            userDTO.setRoles(userEntity.getRoles());
            userDTO.setStatus(userEntity.isStatus());
            userDTOs.add(userDTO);
        }
        return userDTOs;
    }

    @Override
    public List<UserDTO> getAllUsersFalse() {
        List<UserEntity> userEntities = userRepository.findAllByIsDeleteFalseAndStatusFalse();
        List<UserDTO> userDTOs = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(userEntity.getUsername());
            userDTO.setFirstname(userEntity.getFirstname());
            userDTO.setPassword(userEntity.getPassword());
            userDTO.setUserId(userEntity.getUserId());
            userDTO.setLastname(userEntity.getLastname());
            userDTO.setRoles(userEntity.getRoles());
            userDTO.setStatus(userEntity.isStatus());
            userDTOs.add(userDTO);
        }
        return userDTOs;
    }


    @Override
    public ResponseDTO deleteUser(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng"));;

        user.setDelete(true);
        userRepository.save(user);

        return new ResponseDTO(true, "Xóa người dùng thành công!");
    }
    public String getUsernameByToken(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "").trim();

            if (jwtToken.isEmpty()) {
                return "Token is empty";
            }

            Jwt decodedToken = customJWTDecoder.decode(jwtToken);

            String username = decodedToken.getSubject();

            if (username == null) {
                return "Username not found in token";
            }

            return username;
        } catch (JwtException e) {
            System.err.println("Invalid token: " + e.getMessage());
            return "Invalid token: " + e.getMessage();
        } catch (Exception e) {
            // Các lỗi khác
            e.printStackTrace();
            return "An error occurred while processing the token: " + e.getMessage();
        }
    }
    public UUID getUserIdByToken(String token) {
        try {
            String jwtToken = token.replace("Bearer ", "").trim();
            if (jwtToken.isEmpty()) return null;

            Jwt decodedToken = customJWTDecoder.decode(jwtToken);
            String username = decodedToken.getSubject();
            return getUserIdByUsername(username);
        } catch (JwtException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    @Override
    public SellerDTO getSellerByUserId(UUID userId) {
        Optional<SellerDTO> sellerOptional = sellerService.findByUserId(userId);
        return sellerOptional.orElse(null);
    }
    public String getRoleFromToken(String token) {
        try {
            // Lấy UserId từ token
            UUID userId = getUserIdByToken(token);
            if (userId == null) {
                return null; // Nếu không lấy được UserId thì trả về null
            }

            // Truy vấn thông tin người dùng từ database bằng UserId
            Optional<UserEntity> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                UserEntity user = userOptional.get();
                return user.getRoles(); // Trả về role của người dùng
            }

            return null; // Nếu không tìm thấy người dùng thì trả về null
        } catch (Exception e) {
            // Log hoặc xử lý lỗi ở đây nếu cần
            return null;
        }
    }
}
