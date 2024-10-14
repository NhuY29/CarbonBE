package com.example.KLTN.commonCategory;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public interface CommonParentChildService {
    List<CommonParentChildDTO> getAllCategories();
    Optional<CommonParentChildDTO> getCategoryById(UUID id);
    CommonParentChildEntity createCategory(CommonParentChildEntity category);
    CommonParentChildEntity updateCategory(UUID id, CommonParentChildEntity updatedCategory);
    void deleteCategory(UUID id);
    List<CommonParentChildEntity> getAllParentCategories();
    List<CommonParentChildEntity> getAllChildCategoriesByParentId(UUID parentId);
}
