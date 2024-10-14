package com.example.KLTN.commonCategory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommonParentChildImpl implements CommonParentChildService{
    @Autowired
    private CommonParentChildRepository repository;
    @Override
    public List<CommonParentChildDTO> getAllCategories() {
        return repository.findAllCategoryDTOs();
    }
@Override
    public List<CommonParentChildEntity> getAllParentCategories() {
        return repository.findAllParentCategories();
    }
    @Override
    public List<CommonParentChildEntity> getAllChildCategoriesByParentId(UUID parentId) {
        return repository.findAllChildCategoriesByParentId(parentId);
    }

    @Override
    public Optional<CommonParentChildDTO> getCategoryById(UUID id) {
        return repository.findById(id).map(category -> {
            CommonParentChildDTO dto = new CommonParentChildDTO();
            dto.setId(category.getId());
            dto.setName(category.getName());
            dto.setDescription(category.getDescription());
            // Kiểm tra nếu category có parent thì gán parentId
            dto.setParentId(category.getParent() != null ? category.getParent().getId() : null);
            return dto;
        });
    }

    @Override
    public CommonParentChildEntity createCategory(CommonParentChildEntity category) {
        if (category.getParent() != null) {
            category.setParent(repository.findById(category.getParent().getId()).orElse(null));
        }
        return repository.save(category);
    }

    @Override
    public CommonParentChildEntity updateCategory(UUID id, CommonParentChildEntity updatedCategory) {
        return repository.findById(id)
                .map(category -> {
                    category.setName(updatedCategory.getName());
                    category.setDescription(updatedCategory.getDescription());
                    category.setParent(updatedCategory.getParent() != null ? repository.findById(updatedCategory.getParent().getId()).orElse(null) : null);
                    return repository.save(category);
                }).orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    @Override
    public void deleteCategory(UUID id) {
        repository.deleteById(id);
    }
}
