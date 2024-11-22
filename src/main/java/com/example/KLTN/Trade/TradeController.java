package com.example.KLTN.Trade;

import com.example.KLTN.projectManagement.ProjectDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/trade")
public class TradeController {
    @Autowired
    TradeService tradeService;
    @GetMapping("/{tradeId}")
    public ResponseEntity<TradeDTO> getTradeById(@PathVariable UUID tradeId) {
        Optional<TradeDTO> tradeDTO = Optional.ofNullable(tradeService.getTradeById(tradeId));  // Lấy TradeDTO từ service
        return tradeDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/projects/{userId}")
    public List<ProjectDTO> getDistinctProjectDetailsByUserId(@PathVariable UUID userId) {
        return tradeService.getDistinctProjectDetailsByUserId(userId);
    }
    @PutMapping("/{tradeId}/updateTokenAddress")
    public ResponseEntity<Map<String, String>> updateTradeTokenAddress(@PathVariable UUID tradeId,
                                                                       @RequestParam String newTokenAddress) {
        try {
            tradeService.updateTradeTokenAddress(tradeId, newTokenAddress);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Token address updated successfully");
            response.put("details", "Trade ID: " + tradeId);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Trade not found");
            response.put("details", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "An error occurred while updating token address");
            response.put("details", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createTrade(@RequestBody TradeRequest tradeRequest) {
        try {
            TradeEntity createdTrade = tradeService.createTrade(
                    tradeRequest.getBuyerUserId(),
                    tradeRequest.getProjectId(),
                    tradeRequest.getQuantity(),
                    tradeRequest.getMintToken(),
                    tradeRequest.getTokenAddress(),
                    tradeRequest.getPrice(),
                    tradeRequest.getPurchasedFrom(),
                    tradeRequest.getPurchasePrice()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Thêm thành công");
            response.put("success", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            response.put("success", false);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }




    @DeleteMapping("/{tradeId}")
    public ResponseEntity<Map<String, String>> deleteTradeById(@PathVariable UUID tradeId) {
        tradeService.deleteTradeById(tradeId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã xóa Trade với ID: " + tradeId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{tradeId}/quantity")
    public ResponseEntity<Void> updateTradeQuantity(@PathVariable UUID tradeId, @RequestParam int quantity) {
        try {
            tradeService.updateTradeQuantity(tradeId, quantity);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/user")
    public List<TradeDTO> getTradesByUserIdAndMintToken(
            @RequestParam("userId") UUID userId,
            @RequestParam("mintToken") String mintToken) {
        return tradeService.getTradesByUserIdAndMintToken(userId, mintToken);
    }


    @PutMapping("/updateApproval/{tradeId}")
    public ResponseEntity<Void> updateTrade(@PathVariable UUID tradeId,
                                            @RequestParam String newPrice,
                                            @RequestParam String newApprovalStatus,
                                            @RequestParam int newQuantity,
                                            @RequestParam String newStatus) {
        tradeService.updatePriceAndApprovalStatus(tradeId, newPrice, newApprovalStatus, newQuantity, newStatus);
        return ResponseEntity.ok().build();
    }


    @GetMapping
    public ResponseEntity<List<TradeDTO>> getAllTrades() {
        List<TradeDTO> trades = tradeService.getAllTrades();
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/{tradeId}/status")
    public String getTradeStatus(@PathVariable UUID tradeId) {
        return tradeService.getStatusByTradeId(tradeId);
    }
    @PutMapping("/{tradeId}")
    public ResponseEntity<String> deactivateTrade(@PathVariable UUID tradeId) {
        try {
            tradeService.updateTradeStatus(tradeId);
            return ResponseEntity.ok("Cập nhật trạng thái thành công");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trade không tồn tại với ID: " + tradeId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi khi cập nhật trạng thái");
        }
    }

}
