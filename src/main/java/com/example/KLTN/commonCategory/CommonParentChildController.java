package com.example.KLTN.commonCategory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("commonParentChild")
public class CommonParentChildController {
    @Autowired
    private CommonParentChildService commonParentChildService;

    @GetMapping("/all")
    public ResponseEntity<List<CommonParentChildDTO>> getAllCategories() {
        List<CommonParentChildDTO> categories = commonParentChildService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    @GetMapping("/parents")
    public ResponseEntity<List<CommonParentChildEntity>> getAllParentCategories() {
        List<CommonParentChildEntity> categories = commonParentChildService.getAllParentCategories();
        return ResponseEntity.ok(categories);
    }
    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<CommonParentChildEntity>> getAllChildCategoriesByParentId(@PathVariable UUID parentId) {
        List<CommonParentChildEntity> categories = commonParentChildService.getAllChildCategoriesByParentId(parentId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonParentChildDTO> getCategoryById(@PathVariable UUID id) {
        Optional<CommonParentChildDTO> categoryDTO = commonParentChildService.getCategoryById(id);
        return categoryDTO.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CommonParentChildEntity> createCategory(@RequestBody CommonParentChildEntity category) {
        CommonParentChildEntity newCategory = commonParentChildService.createCategory(category);
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonParentChildEntity> updateCategory(@PathVariable UUID id, @RequestBody CommonParentChildEntity updatedCategory) {
        CommonParentChildEntity updated = commonParentChildService.updateCategory(id, updatedCategory);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        commonParentChildService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
