package com.example.KLTN.Trade;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trade")
public class TradeController {
    @Autowired
    TradeService tradeService;
    @GetMapping("/{tradeId}")
    public ResponseEntity<TradeEntity> getTradeById(@PathVariable UUID tradeId) {
        Optional<TradeEntity> tradeEntity = tradeService.getTradeById(tradeId);
        return tradeEntity.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping()
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
