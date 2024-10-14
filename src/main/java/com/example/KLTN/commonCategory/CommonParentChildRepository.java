package com.example.KLTN.commonCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CommonParentChildRepository extends JpaRepository<CommonParentChildEntity,UUID> {
    @Query("SELECT c FROM CommonParentChildEntity c WHERE c.parent IS NULL")
    List<CommonParentChildEntity> findAllParentCategories();

    // Lấy danh mục con theo parentId
    @Query("SELECT c FROM CommonParentChildEntity c WHERE c.parent.id = :parentId")
    List<CommonParentChildEntity> findAllChildCategoriesByParentId(UUID parentId);

    @Query("SELECT new com.example.KLTN.commonCategory.CommonParentChildDTO(c.id, c.name, c.description, c.parent.id) FROM CommonParentChildEntity c")
    List<CommonParentChildDTO> findAllCategoryDTOs();
}
