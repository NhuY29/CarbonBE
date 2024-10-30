package com.example.KLTN.Trade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeService {
    Optional<TradeEntity> getTradeById(UUID tradeId);
    List<TradeDTO> getAllTrades();
}
