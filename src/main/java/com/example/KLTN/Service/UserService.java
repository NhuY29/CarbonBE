package com.example.KLTN.Service;
import com.example.KLTN.Buyer.BuyerDTO;
import com.example.KLTN.DTO.MailDTO;
import com.example.KLTN.DTO.ResponseDTO;
import com.example.KLTN.DTO.UserDTO;
import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Seller.SellerDTO;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.UUID;

public interface UserService {
    ResponseDTO createUser(UserDTO userDTO, BuyerDTO buyerDTO, SellerDTO sellerDTO);
    List<UserDTO> getAllUsersTrue();
    List<UserDTO> getAllUsersFalse();
    ResponseDTO deleteUser(UUID id);
    ResponseDTO updateUserStatus(UUID userId, boolean status);
    void sendHtmlMail(MailDTO dataMail,String templateName) throws MessagingException;
    String getUsernameById(UUID id);
     Page<UserEntity> getAll(Integer pageNo, Integer pageSize);
    Page<UserEntity> searchUsers(String searchTerm, int page, int size);
    UUID getUserIdByUsername(String username);
    String getUsernameByToken(@RequestHeader("Authorization") String token);
    UUID getUserIdByToken(String token);
    SellerDTO getSellerByUserId(UUID userId);
}
