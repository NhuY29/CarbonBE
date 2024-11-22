package com.example.KLTN.Cart;



import java.util.List;
import java.util.UUID;

public interface CartService {
    CartEntity addToCart(UUID userId, UUID tradeId, int amount);
    void deleteCartItem(UUID cartId);
    List<CartDTO> getAllCartItemsByUserId(UUID userId);
}
