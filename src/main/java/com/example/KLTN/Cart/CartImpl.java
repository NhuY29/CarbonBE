package com.example.KLTN.Cart;

import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Reponsitory.UserRepository;
import com.example.KLTN.Trade.TradeDTO;
import com.example.KLTN.Trade.TradeEntity;
import com.example.KLTN.Trade.TradeRepository;
import com.example.KLTN.projectManagement.ImageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartImpl implements CartService {

    @Autowired
    private CartReponsitory cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Override
    public CartEntity addToCart(UUID userId, UUID tradeId, int amount) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<CartEntity> existingCartItems = cartRepository.findByUser_UserIdAndTradeId(userId, tradeId);

        if (!existingCartItems.isEmpty()) {
            CartEntity existingCartItem = existingCartItems.get(0);
            existingCartItem.setAmount(existingCartItem.getAmount() + amount);
            return cartRepository.save(existingCartItem);
        } else {
            CartEntity cartItem = new CartEntity();
            cartItem.setTradeId(tradeId);
            cartItem.setUser(user);
            cartItem.setAmount(amount);

            return cartRepository.save(cartItem);
        }
    }

    @Override
    public List<CartDTO> getAllCartItemsByUserId(UUID userId) {
        List<CartEntity> cartItems = cartRepository.findByUser_UserId(userId);
        return cartItems.stream()
                .map(cartItem -> {
                    Optional<TradeEntity> tradeOpt = tradeRepository.findById(cartItem.getTradeId());
                    if (!tradeOpt.isPresent() || !"true".equals(tradeOpt.get().getStatus())) {
                        return null;
                    }

                    TradeEntity trade = tradeOpt.get();
                    CartDTO cartDTO = new CartDTO();
                    cartDTO.setCartId(cartItem.getCartId());
                    cartDTO.setTradeId(trade.getTradeId().toString());
                    cartDTO.setProjectId(trade.getProject() != null ? trade.getProject().getProjectId().toString() : null);
                    cartDTO.setProjectName(trade.getProjectName());
                    cartDTO.setField(trade.getField());
                    cartDTO.setCompanyName(trade.getCompanyName());
                    cartDTO.setQuantity(cartItem.getAmount());
                    cartDTO.setPrice(trade.getPrice());
                    cartDTO.setMintToken(trade.getMintToken());
                    cartDTO.setStandardId(trade.getStandardId() != null ? trade.getStandardId().toString() : null);
                    cartDTO.setTypeId(trade.getTypeId() != null ? trade.getTypeId().toString() : null);
                    cartDTO.setProjectDescription(trade.getProjectDescription());
                    cartDTO.setTypeName(trade.getTypeName());
                    cartDTO.setStandardName(trade.getStandardName());

                    List<String> imageUrls = trade.getProject() != null ?
                            trade.getProject().getImages().stream()
                                    .map(ImageEntity::getUrl)
                                    .collect(Collectors.toList()) : new ArrayList<>();

                    cartDTO.setImageUrls(imageUrls);

                    cartDTO.setUserId(trade.getUserId());
                    cartDTO.setTokenAddress(trade.getTokenAddress());
                    cartDTO.setAmount(cartItem.getAmount());

                    return cartDTO;
                })
                .filter(cartDTO -> cartDTO != null)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCartItem(UUID cartId) {
        CartEntity cartItem = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        cartRepository.delete(cartItem);
    }
}
