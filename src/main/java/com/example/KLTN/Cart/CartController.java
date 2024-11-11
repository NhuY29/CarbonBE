package com.example.KLTN.Cart;

import com.example.KLTN.DTO.ResponseDTO;
import com.example.KLTN.Trade.TradeDTO;
import com.example.KLTN.Trade.TradeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<ResponseDTO> addToCart(
            @RequestParam UUID userId,
            @RequestParam UUID tradeId,
            @RequestParam int amount) {
        try {
            CartEntity cartItem = cartService.addToCart(userId, tradeId, amount);
            ResponseDTO response = new ResponseDTO(true, "Đã thêm vào giỏ hàng thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseDTO response = new ResponseDTO(false, "Thất bại: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CartDTO>> getAllCartItemsByUserId(@PathVariable UUID userId) {
        // Gọi dịch vụ để lấy danh sách CartDTO
        List<CartDTO> cartItems = cartService.getAllCartItemsByUserId(userId);
        // Trả về danh sách CartDTO với mã phản hồi OK
        return ResponseEntity.ok(cartItems);
    }


    @DeleteMapping("delete/{cartId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable UUID cartId) {
        cartService.deleteCartItem(cartId);
        return ResponseEntity.noContent().build();
    }

}
