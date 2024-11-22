package com.example.KLTN.Trade;

import com.example.KLTN.projectManagement.ProjectDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TradeService {
    TradeDTO getTradeById(UUID tradeId) ;
    List<TradeDTO> getAllTrades();
    String getStatusByTradeId(UUID tradeId);
    void updateTradeStatus(UUID tradeId);
    void updatePriceAndApprovalStatus(UUID tradeId, String newPrice, String newApprovalStatus, int newQuantity, String newStatus);
    List<ProjectDTO> getDistinctProjectDetailsByUserId(UUID userId);
    void updateTradeQuantity(UUID tradeId, int newQuantity);
    void deleteTradeById(UUID tradeId);
    TradeEntity createTrade(UUID buyerUserId, UUID projectId, int quantity, String mintToken, String tokenAddress, String price, String purchasedFrom, BigDecimal purchasePrice);
    void updateTradeTokenAddress(UUID tradeId, String newTokenAddress);
    List<TradeDTO> getTradesByUserIdAndMintToken(UUID userId, String mintToken);
}
