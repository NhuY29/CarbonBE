package com.example.KLTN.commonCategory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("common-categories")
public class CommonCategoryController {
    @Autowired
    private CommonCategoryService commonCategoryService;

    @GetMapping
    public List<CommonCategoryDTO> getAllCategories() {
        return commonCategoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public CommonCategoryDTO getCategoryById(@PathVariable UUID id) {
        return commonCategoryService.getCategoryById(id);
    }

    @PostMapping
    public CommonCategoryDTO createCategory(@RequestBody CommonCategoryDTO categoryDTO) {
        return commonCategoryService.createCategory(categoryDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonCategoryDTO> updateCategory(
            @PathVariable UUID id,
            @RequestBody CommonCategoryDTO categoryDTO) {
        CommonCategoryDTO updatedCategory = commonCategoryService.updateCategory(id, categoryDTO);
        if (updatedCategory != null) {
            return ResponseEntity.ok(updatedCategory);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable UUID id) {
        commonCategoryService.deleteCategory(id);
    }

    @GetMapping("/by-category-loai-hinh")
    public List<CommonCategoryDTO> getCategoriesByCategoryLoaiHinh() {
        return commonCategoryService.getCategoriesByCategoryLoaiHinh();
    }

    @GetMapping("/by-category-tieu-chuan")
    public List<CommonCategoryDTO> getCategoriesByCategoryTieuChuan() {
        return commonCategoryService.getCategoriesByCategoryTieuChuan();
    }

    @GetMapping("/by-category-chat")
    public List<CommonCategoryDTO> getCategoriesByCategoryChat() {
        return commonCategoryService.getCategoriesByCategoryChat();
    }
    @GetMapping("/search")
    public List<CommonCategoryDTO> searchCategories(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category) {
        return commonCategoryService.searchCategories(name, category);
    }
}
