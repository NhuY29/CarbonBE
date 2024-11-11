package com.example.KLTN.Trade;

import com.example.KLTN.commonCategory.CommonCategoryEntity;
import com.example.KLTN.commonCategory.CommonCategoryRepository;
import com.example.KLTN.projectManagement.ImageEntity;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TradeImpl implements TradeService {
    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private CommonCategoryRepository commonCategoryRepository;
    @Override
    public void updateTradeStatus(UUID tradeId) {
        TradeEntity tradeEntity = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new EntityNotFoundException("Trade không tồn tại với ID: " + tradeId));

        // Cập nhật status thành "false"
        tradeEntity.setStatus("false");

        // Lưu lại cập nhật
        tradeRepository.save(tradeEntity);
    }

    @Override
    public Optional<TradeEntity> getTradeById(UUID tradeId) {
        return tradeRepository.findById(tradeId);
    }
    @Override
    public String getStatusByTradeId(UUID tradeId) {
        Optional<TradeEntity> tradeOptional = tradeRepository.findById(tradeId);
        if (tradeOptional.isPresent()) {
            return tradeOptional.get().getStatus();
        } else {
            return "Trade không tồn tại"; // Hoặc giá trị mặc định khác nếu trade không tìm thấy
        }
    }
    @Override
    public List<TradeDTO> getAllTrades() {
        return tradeRepository.findAll().stream()
                .filter(trade -> "true".equals(trade.getStatus()))
                .map(trade -> {
                    Optional<CommonCategoryEntity> typeEntity = Optional.ofNullable(trade.getTypeId())
                            .flatMap(commonCategoryRepository::findById);

                    String typeName = typeEntity.map(CommonCategoryEntity::getName).orElse("Unknown");
                    String typeId = typeEntity.map(CommonCategoryEntity::getId).map(UUID::toString).orElse(null);
                    Optional<CommonCategoryEntity> standardEntity = Optional.ofNullable(trade.getStandardId())
                            .flatMap(commonCategoryRepository::findById);

                    String standardName = standardEntity.map(CommonCategoryEntity::getName).orElse("Unknown");
                    String standardId = standardEntity.map(CommonCategoryEntity::getId).map(UUID::toString).orElse(null);
                    String projectId = trade.getProject().getProjectId().toString();
                    List<String> imageUrls = trade.getProject().getImages().stream()
                            .map(ImageEntity::getUrl)
                            .collect(Collectors.toList());
                    String tokenAddress = trade.getTokenAddress();
                    String tradeId = String.valueOf(trade.getTradeId());
                    String status = trade.getStatus();

                    return new TradeDTO(
                            tradeId, // Thêm tradeId vào constructor
                            projectId,
                            trade.getProjectName(),
                            trade.getField(),
                            trade.getCompanyName(),
                            trade.getQuantity(),
                            trade.getPrice(),
                            trade.getMintToken(),
                            standardId,
                            typeId,
                            trade.getProjectDescription(),
                            typeName,
                            standardName,
                            imageUrls,
                            trade.getUserId(),
                            tokenAddress,
                            status // Thêm status vào constructor
                    );
                }).collect(Collectors.toList());
    }



}
