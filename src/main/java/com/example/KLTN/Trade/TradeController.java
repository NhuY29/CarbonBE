package com.example.KLTN.Trade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
}
