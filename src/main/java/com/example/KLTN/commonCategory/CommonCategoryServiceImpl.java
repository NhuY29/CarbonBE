package com.example.KLTN.commonCategory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class CommonCategoryServiceImpl implements CommonCategoryService {
    @Autowired
    private CommonCategoryRepository commonCategoryRepository;

    @Override
    public List<CommonCategoryDTO> getAllCategories() {
        return commonCategoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public CommonCategoryDTO getCategoryById(UUID id) {
        Optional<CommonCategoryEntity> entity = commonCategoryRepository.findById(id);
        return entity.map(this::convertToDTO).orElse(null);
    }

    @Override
    public CommonCategoryDTO createCategory(CommonCategoryDTO categoryDTO) {
        CommonCategoryEntity entity = convertToEntity(categoryDTO);
        CommonCategoryEntity savedEntity = commonCategoryRepository.save(entity);
        return convertToDTO(savedEntity);
    }

    private CommonCategoryEntity convertToEntity(CommonCategoryDTO categoryDTO) {
        CommonCategoryEntity entity = new CommonCategoryEntity();
        entity.setCode(categoryDTO.getCode());
        entity.setName(categoryDTO.getName());
        entity.setDescription(categoryDTO.getDescription());
        entity.setCategory(categoryDTO.getCategory());
        entity.setUnit(categoryDTO.getUnit());
        entity.setConversionPrice(categoryDTO.getConversionPrice());
        return entity;
    }

    @Override
    public void deleteCategory(UUID id) {
        commonCategoryRepository.deleteById(id);
    }

    @Override
    public CommonCategoryDTO updateCategory(UUID id, CommonCategoryDTO categoryDTO) {
        Optional<CommonCategoryEntity> existingCategory = commonCategoryRepository.findById(id);
        if (existingCategory.isPresent()) {
            CommonCategoryEntity entity = existingCategory.get();
            entity.setCode(categoryDTO.getCode());
            entity.setCategory(categoryDTO.getCategory());
            entity.setName(categoryDTO.getName());
            entity.setDescription(categoryDTO.getDescription());
            entity.setUnit(categoryDTO.getUnit());
            entity.setConversionPrice(categoryDTO.getConversionPrice());

            CommonCategoryEntity updatedEntity = commonCategoryRepository.save(entity);
            return convertToDTO(updatedEntity);
        } else {
            return null;
        }
    }

    private CommonCategoryDTO convertToDTO(CommonCategoryEntity entity) {
        CommonCategoryDTO dto = new CommonCategoryDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setUnit(entity.getUnit());
        dto.setConversionPrice(entity.getConversionPrice());
        return dto;
    }

    @Override
    public List<CommonCategoryDTO> getCategoriesByCategoryLoaiHinh() {
        return commonCategoryRepository.findByCategoryLoaiHinh().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public List<CommonCategoryDTO> getCategoriesByCategoryTieuChuan() {
        return commonCategoryRepository.findByCategoryTieuChuan().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public List<CommonCategoryDTO> getCategoriesByCategoryChat() {
        return commonCategoryRepository.findByCategoryChat().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public List<CommonCategoryDTO> searchCategories(String name, String category) {
        List<CommonCategoryEntity> entities = commonCategoryRepository.searchByNameAndCategory(name, category);
        return entities.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}
