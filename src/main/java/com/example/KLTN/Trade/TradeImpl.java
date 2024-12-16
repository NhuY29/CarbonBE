package com.example.KLTN.Trade;

import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Enum.Status;
import com.example.KLTN.Reponsitory.UserRepository;
import com.example.KLTN.commonCategory.CommonCategoryEntity;
import com.example.KLTN.commonCategory.CommonCategoryRepository;
import com.example.KLTN.projectManagement.ImageEntity;
import com.example.KLTN.projectManagement.ProjectDTO;
import com.example.KLTN.projectManagement.ProjectEntity;
import com.example.KLTN.projectManagement.ProjectReponsitory;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TradeImpl implements TradeService {
    @Autowired
    private TradeRepository tradeRepository;
    @Autowired
    private ProjectReponsitory projectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommonCategoryRepository commonCategoryRepository;
    @Autowired
    private Trade2Repository trade2Repository;
    @Override
    public void deleteTradeById(UUID tradeId) {
        TradeEntity tradeEntity = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new EntityNotFoundException("Trade không tồn tại với ID: " + tradeId));

        tradeRepository.delete(tradeEntity);
    }
    @Override
    public void updateTradeStatus(UUID tradeId) {
        TradeEntity tradeEntity = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new EntityNotFoundException("Trade không tồn tại với ID: " + tradeId));

        tradeEntity.setStatus("false");

        tradeRepository.save(tradeEntity);
    }
    @Override
    public void updateTradeQuantity(UUID tradeId, int newQuantity) {
        TradeEntity tradeEntity = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new EntityNotFoundException("Trade không tồn tại với ID: " + tradeId));

        tradeEntity.setQuantity(newQuantity);

        tradeRepository.save(tradeEntity);
    }

    @Override
    public TradeDTO getTradeById(UUID tradeId) {
        TradeEntity tradeEntity = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new EntityNotFoundException("Trade không tồn tại với ID: " + tradeId));

        Optional<CommonCategoryEntity> typeEntity = Optional.ofNullable(tradeEntity.getTypeId())
                .flatMap(commonCategoryRepository::findById);

        String typeName = typeEntity.map(CommonCategoryEntity::getName).orElse("Unknown");
        String typeId = typeEntity.map(CommonCategoryEntity::getId).map(UUID::toString).orElse(null);

        Optional<CommonCategoryEntity> standardEntity = Optional.ofNullable(tradeEntity.getStandardId())
                .flatMap(commonCategoryRepository::findById);

        String standardName = standardEntity.map(CommonCategoryEntity::getName).orElse("Unknown");
        String standardId = standardEntity.map(CommonCategoryEntity::getId).map(UUID::toString).orElse(null);

        String projectId = tradeEntity.getProject().getProjectId().toString();
        List<String> imageUrls = tradeEntity.getProject().getImages().stream()
                .map(ImageEntity::getUrl)
                .collect(Collectors.toList());

        String tokenAddress = tradeEntity.getTokenAddress();
        String tradeIdStr = String.valueOf(tradeEntity.getTradeId());
        String status = tradeEntity.getStatus();
        String approvalStatus = tradeEntity.getApprovalStatus() != null ? tradeEntity.getApprovalStatus().name() : "Unknown";

        return new TradeDTO(
                tradeIdStr,
                projectId,
                tradeEntity.getProjectName(),
                tradeEntity.getField(),
                tradeEntity.getCompanyName(),
                tradeEntity.getQuantity(),
                tradeEntity.getPrice(),
                tradeEntity.getMintToken(),
                standardId,
                typeId,
                tradeEntity.getProjectDescription(),
                typeName,
                standardName,
                imageUrls,
                tradeEntity.getUserId(),
                tokenAddress,
                status, // Status
                approvalStatus ,
                tradeEntity.getPurchasedFrom(),
                tradeEntity.getPurchasePrice().toString()
        );
    }
    @Override
    public List<ProjectDTO> getDistinctProjectDetailsByUserId(UUID userId) {
        List<TradeEntity> trades = tradeRepository.findAllByUserId(userId);

        List<UUID> projectIds = trades.stream()
                .map(trade -> trade.getProject().getProjectId())
                .distinct()
                .toList();

        List<ProjectEntity> projects = projectRepository.findAllByProjectIdIn(projectIds);

        return projects.stream()
                .map(project -> new ProjectDTO(project.getProjectId(), project.getProjectName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getStatusByTradeId(UUID tradeId) {
        Optional<TradeEntity> tradeOptional = tradeRepository.findById(tradeId);
        if (tradeOptional.isPresent()) {
            return tradeOptional.get().getStatus();
        } else {
            return "Trade không tồn tại";
        }
    }
    @Override
    public List<TradeDTO> getTradesByUserIdAndMintToken(UUID userId, String mintToken) {
        List<TradeDTO> trades = tradeRepository.findAllByUserId(userId).stream()
                .filter(trade -> trade.getMintToken().equals(mintToken))
                .map(trade -> {
                    Optional<CommonCategoryEntity> typeEntity = Optional.ofNullable(trade.getTypeId())
                            .flatMap(commonCategoryRepository::findById);

                    String typeName = typeEntity.map(CommonCategoryEntity::getName).orElse("Unknown");
                    String typeId = typeEntity.map(CommonCategoryEntity::getId).map(UUID::toString).orElse(null);

                    Optional<CommonCategoryEntity> standardEntity = Optional.ofNullable(trade.getStandardId())
                            .flatMap(commonCategoryRepository::findById);

                    String standardName = standardEntity.map(CommonCategoryEntity::getName).orElse("Unknown");
                    String standardId = standardEntity.map(CommonCategoryEntity::getId).map(UUID::toString).orElse(null);

                    String projectIdStr = trade.getProject().getProjectId().toString();

                    List<String> imageUrls = trade.getProject().getImages().stream()
                            .map(ImageEntity::getUrl)
                            .collect(Collectors.toList());
                    String tokenAddress = trade.getTokenAddress();
                    String tradeId = String.valueOf(trade.getTradeId());
                    String status = trade.getStatus();
                    String approvalStatus = trade.getApprovalStatus() != null ? trade.getApprovalStatus().name() : "Unknown";
                    return new TradeDTO(
                            tradeId,
                            projectIdStr,
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
                            status,
                            approvalStatus,
                            trade.getPurchasedFrom(),
                            trade.getPurchasePrice().toString()
                    );
                })
                .collect(Collectors.toList());

        return trades;
    }


    @Override
    public void updatePriceAndApprovalStatus(UUID tradeId, String newPrice, String newApprovalStatus, int newQuantity, String newStatus) {
        TradeEntity tradeEntity = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new EntityNotFoundException("Trade không tồn tại với ID: " + tradeId));
        tradeEntity.setPrice(newPrice);
        try {
            Status approvalStatus = Status.valueOf(newApprovalStatus);
            tradeEntity.setApprovalStatus(approvalStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái phê duyệt không hợp lệ: " + newApprovalStatus);
        }
        tradeEntity.setQuantity(newQuantity);
        tradeEntity.setStatus(newStatus);

        tradeRepository.save(tradeEntity);
    }

    @Override
    public List<TradeDTO> getAllTrades() {
        return tradeRepository.findAll().stream()
                .filter(trade -> "true".equals(trade.getStatus()) && Status.DAXULY.equals(trade.getApprovalStatus()))
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
                    String approvalStatus = trade.getApprovalStatus() != null ? trade.getApprovalStatus().name() : "Unknown"; // Lấy approvalStatus

                    return new TradeDTO(
                            tradeId,
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
                            status,
                            approvalStatus ,
                            trade.getPurchasedFrom(),
                            trade.getPurchasePrice().toString()
                    );
                })
                .collect(Collectors.toList());
    }
    public TradeEntity createTrade(UUID buyerUserId, UUID projectId, int quantity, String mintToken,
                                   String tokenAddress, String price, String purchasedFrom, BigDecimal purchasePrice) {
        try {
            ProjectEntity project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Project không tồn tại"));
            Optional<Trade2Entity> existingTrade2 = trade2Repository.findByMintTokenAndProject_ProjectIdAndUserId(mintToken, projectId, buyerUserId);

            TradeEntity tradeEntity = new TradeEntity();
            tradeEntity.setProject(project);
            tradeEntity.setProjectName(project.getProjectName());
            tradeEntity.setField(project.getField());
            tradeEntity.setCompanyName("");
            tradeEntity.setProjectDescription(project.getProjectDescription());

            UUID typeId = UUID.fromString(project.getType());
            Optional<CommonCategoryEntity> typeEntity = commonCategoryRepository.findById(typeId);
            String typeName = typeEntity.map(CommonCategoryEntity::getName).orElse("Unknown");
            UUID standardId = UUID.fromString(project.getStandard());
            Optional<CommonCategoryEntity> standardEntity = commonCategoryRepository.findById(standardId);
            String standardName = standardEntity.map(CommonCategoryEntity::getName).orElse("Unknown");

            tradeEntity.setStandardId(standardId);
            tradeEntity.setTypeId(typeId);
            tradeEntity.setQuantity(quantity);
            tradeEntity.setMintToken(mintToken);
            tradeEntity.setTokenAddress(tokenAddress);
            tradeEntity.setPrice(price);
            tradeEntity.setUserId(buyerUserId);
            tradeEntity.setStatus("true");
            tradeEntity.setStandardName(standardName);
            tradeEntity.setTypeName(typeName);
            tradeEntity.setApprovalStatus(Status.CHOXULY);

            tradeEntity.setPurchasedFrom(purchasedFrom);
            tradeEntity.setPurchasePrice(String.valueOf(purchasePrice));
            if (existingTrade2.isPresent()) {
                Trade2Entity trade2Entity = existingTrade2.get();
                trade2Entity.setQuantity(trade2Entity.getQuantity() + quantity);
                trade2Repository.save(trade2Entity);
            } else {
                // Nếu không có giao dịch cũ, tạo mới Trade2Entity
                Trade2Entity trade2Entity = new Trade2Entity();
                trade2Entity.setProject(project);
                trade2Entity.setProjectName(project.getProjectName());
                trade2Entity.setField(project.getField());
                trade2Entity.setProjectDescription(project.getProjectDescription());

                trade2Entity.setStandardId(standardId);
                trade2Entity.setTypeId(typeId);
                trade2Entity.setQuantity(quantity);
                trade2Entity.setMintToken(mintToken);
                trade2Entity.setUserId(buyerUserId);
                trade2Entity.setStandardName(standardName);
                trade2Entity.setTypeName(typeName);
                trade2Repository.save(trade2Entity);
            }
            tradeEntity = tradeRepository.save(tradeEntity);
            return tradeEntity;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo giao dịch: " + e.getMessage(), e);
        }
    }



    @Override
    public void updateTradeTokenAddress(UUID tradeId, String newTokenAddress) {

        TradeEntity tradeEntity = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new EntityNotFoundException("Trade không tồn tại với ID: " + tradeId));
        tradeEntity.setTokenAddress(newTokenAddress);

        tradeRepository.save(tradeEntity);
    }

}
