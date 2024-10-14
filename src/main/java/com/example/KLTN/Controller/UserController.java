package com.example.KLTN.Controller;

import com.example.KLTN.DTO.ResponseDTO;
import com.example.KLTN.DTO.UserDTO;
import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Request.RegisterRequest;
import com.example.KLTN.Seller.SellerDTO;
import com.example.KLTN.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
@PostMapping("/register")
public ResponseDTO createUser(@RequestBody RegisterRequest registrationDTO) {
    return userService.createUser(registrationDTO.getUserDTO(), registrationDTO.getBuyerDTO(), registrationDTO.getSellerDTO());
}

    @GetMapping("/getAll")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsersTrue();
    }
    @GetMapping("/getAll2")
    public List<UserDTO> getAllUsers2() {
        return userService.getAllUsersFalse();
    }
    @DeleteMapping("delete/{id}")
    public ResponseDTO deleteUser(@PathVariable("id") UUID id) {
        return userService.deleteUser(id);
    }
    @PutMapping("/{userId}")
    public ResponseEntity<ResponseDTO> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam boolean status) {
        ResponseDTO response = userService.updateUserStatus(userId, status);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/getEmail/{id}")
    public ResponseEntity<String> getUsernameById(@PathVariable UUID id) {
        String username = userService.getUsernameById(id);
        if (username != null) {
            return ResponseEntity.ok(username);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/pagination")
    public Page<UserEntity> getAll(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "2") Integer pageSize) {
        return userService.getAll(pageNo, pageSize);
    }
    @GetMapping("/pagination2")
    public Page<UserEntity> searchUsers(
            @RequestParam String term,
            @RequestParam int page,
            @RequestParam int size) {
        return userService.searchUsers(term, page, size);
    }
    @GetMapping("/getusername")
    public String getUsernameFromToken(@RequestHeader("Authorization") String token) {
        // Gọi service để lấy username từ token
        String username = userService.getUsernameByToken(token);

        // Trả về username hoặc thông báo lỗi nếu không lấy được
        if (username != null) {
            return  username;
        } else {
            return "Invalid token or unable to extract username";
        }
    }
    @GetMapping("/getuserid")
    public UUID getUserIdFromToken(@RequestHeader("Authorization") String token) {
        return userService.getUserIdByToken(token);
    }
    @GetMapping("/seller")
    public ResponseEntity<SellerDTO> getSellerInfo(@RequestHeader("Authorization") String token) {
        UUID userId = userService.getUserIdByToken(token);
        if (userId != null) {
            SellerDTO sellerDTO = userService.getSellerByUserId(userId);
            return sellerDTO != null ? ResponseEntity.ok(sellerDTO) : ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


}
