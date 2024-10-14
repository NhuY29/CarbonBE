package com.example.KLTN.commonCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommonCategoryRepository extends JpaRepository<CommonCategoryEntity, UUID> {
    @Query("SELECT c FROM CommonCategoryEntity c WHERE c.category = 'LOAI_HINH'")
    List<CommonCategoryEntity> findByCategoryLoaiHinh();
    @Query("SELECT c FROM CommonCategoryEntity c WHERE c.category = 'TIEU_CHUAN'")
    List<CommonCategoryEntity> findByCategoryTieuChuan();

    @Query("SELECT c FROM CommonCategoryEntity c WHERE c.category = 'CHAT'")
    List<CommonCategoryEntity> findByCategoryChat();

    @Query("SELECT c FROM CommonCategoryEntity c WHERE " +
            "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:category IS NULL OR LOWER(c.category) LIKE LOWER(CONCAT('%', :category, '%')))")
    List<CommonCategoryEntity> searchByNameAndCategory(@Param("name") String name,
                                                       @Param("category") String category);
}