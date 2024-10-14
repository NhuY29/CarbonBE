package com.example.KLTN.commonCategory;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface CommonCategoryService {
    List<CommonCategoryDTO> getAllCategories();
    CommonCategoryDTO getCategoryById(UUID id);
    CommonCategoryDTO createCategory(CommonCategoryDTO categoryDTO);
    void deleteCategory(UUID id);
    CommonCategoryDTO updateCategory(UUID id, CommonCategoryDTO categoryDTO);
    List<CommonCategoryDTO> getCategoriesByCategoryLoaiHinh();
    List<CommonCategoryDTO> getCategoriesByCategoryTieuChuan();
    List<CommonCategoryDTO> getCategoriesByCategoryChat();
    List<CommonCategoryDTO> searchCategories(String name, String category);
}
