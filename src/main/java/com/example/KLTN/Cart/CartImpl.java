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
        // Lấy thông tin người dùng
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Tìm sản phẩm trong giỏ hàng với cùng tradeId
        List<CartEntity> existingCartItems = cartRepository.findByUser_UserIdAndTradeId(userId, tradeId);

        if (!existingCartItems.isEmpty()) {
            // Nếu đã có sản phẩm với cùng tradeId, cập nhật số lượng
            CartEntity existingCartItem = existingCartItems.get(0); // Giả sử chỉ có một sản phẩm với tradeId này
            existingCartItem.setAmount(existingCartItem.getAmount() + amount); // Cập nhật số lượng
            return cartRepository.save(existingCartItem); // Lưu lại cập nhật
        } else {
            // Nếu không có, tạo sản phẩm mới
            CartEntity cartItem = new CartEntity();
            cartItem.setTradeId(tradeId);
            cartItem.setUser(user);
            cartItem.setAmount(amount);

            return cartRepository.save(cartItem);
        }
    }

    @Override
    public List<CartDTO> getAllCartItemsByUserId(UUID userId) {
        // Lấy danh sách sản phẩm trong giỏ hàng của người dùng
        List<CartEntity> cartItems = cartRepository.findByUser_UserId(userId);

        // Lọc và chuyển đổi CartEntity thành CartDTO
        return cartItems.stream()
                .map(cartItem -> {
                    // Lấy thông tin TradeEntity tương ứng với tradeId
                    Optional<TradeEntity> tradeOpt = tradeRepository.findById(cartItem.getTradeId());
                    if (!tradeOpt.isPresent() || !"true".equals(tradeOpt.get().getStatus())) {
                        return null; // Bỏ qua nếu trade không tồn tại hoặc trạng thái không phải "true"
                    }

                    TradeEntity trade = tradeOpt.get();
                    CartDTO cartDTO = new CartDTO();
                    cartDTO.setCartId(cartItem.getCartId());
                    cartDTO.setTradeId(trade.getTradeId().toString());
                    cartDTO.setProjectId(trade.getProject() != null ? trade.getProject().getProjectId().toString() : null);
                    cartDTO.setProjectName(trade.getProjectName());
                    cartDTO.setField(trade.getField());
                    cartDTO.setCompanyName(trade.getCompanyName());
                    cartDTO.setQuantity(cartItem.getAmount()); // Số lượng
                    cartDTO.setPrice(trade.getPrice());
                    cartDTO.setMintToken(trade.getMintToken());
                    cartDTO.setStandardId(trade.getStandardId() != null ? trade.getStandardId().toString() : null);
                    cartDTO.setTypeId(trade.getTypeId() != null ? trade.getTypeId().toString() : null);
                    cartDTO.setProjectDescription(trade.getProjectDescription());
                    cartDTO.setTypeName(trade.getTypeName());
                    cartDTO.setStandardName(trade.getStandardName());

                    // Lấy hình ảnh từ ProjectEntity
                    List<String> imageUrls = trade.getProject() != null ?
                            trade.getProject().getImages().stream()
                                    .map(ImageEntity::getUrl)
                                    .collect(Collectors.toList()) : new ArrayList<>();

                    cartDTO.setImageUrls(imageUrls);

                    // Thiết lập thông tin người dùng và tokenAddress
                    cartDTO.setUserId(trade.getUserId());
                    cartDTO.setTokenAddress(trade.getTokenAddress());

                    // Thiết lập amount từ CartEntity vào CartDTO
                    cartDTO.setAmount(cartItem.getAmount());

                    return cartDTO;
                })
                .filter(cartDTO -> cartDTO != null) // Loại bỏ các CartDTO null
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCartItem(UUID cartId) {
        // Kiểm tra xem cartItem có tồn tại hay không
        CartEntity cartItem = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        // Xóa mục giỏ hàng
        cartRepository.delete(cartItem);
    }
}
