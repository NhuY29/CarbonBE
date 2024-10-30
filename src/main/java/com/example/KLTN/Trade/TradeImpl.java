package com.example.KLTN.Trade;

import com.example.KLTN.commonCategory.CommonCategoryEntity;
import com.example.KLTN.commonCategory.CommonCategoryRepository;
import com.example.KLTN.projectManagement.ImageEntity;
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
    public Optional<TradeEntity> getTradeById(UUID tradeId) {
        return tradeRepository.findById(tradeId);
    }

    @Override
    public List<TradeDTO> getAllTrades() {
        return tradeRepository.findAll().stream().map(trade -> {
            // Lấy tên và ID cho loại
            Optional<CommonCategoryEntity> typeEntity = Optional.ofNullable(trade.getTypeId())
                    .flatMap(commonCategoryRepository::findById);

            String typeName = typeEntity.map(CommonCategoryEntity::getName).orElse("Unknown");
            String typeId = typeEntity.map(CommonCategoryEntity::getId).map(UUID::toString).orElse(null);

            // Lấy tên và ID cho tiêu chuẩn
            Optional<CommonCategoryEntity> standardEntity = Optional.ofNullable(trade.getStandardId())
                    .flatMap(commonCategoryRepository::findById);

            String standardName = standardEntity.map(CommonCategoryEntity::getName).orElse("Unknown");
            String standardId = standardEntity.map(CommonCategoryEntity::getId).map(UUID::toString).orElse(null);

            // Thêm projectId và danh sách hình ảnh vào DTO
            String projectId = trade.getProject().getProjectId().toString();
            List<String> imageUrls = trade.getProject().getImages().stream()
                    .map(ImageEntity::getUrl)
                    .collect(Collectors.toList());

            return new TradeDTO(
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
                    imageUrls // Thêm danh sách URL hình ảnh
            );
        }).collect(Collectors.toList());
    }

}
