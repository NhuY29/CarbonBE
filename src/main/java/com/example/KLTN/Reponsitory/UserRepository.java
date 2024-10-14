package com.example.KLTN.Reponsitory;


import com.example.KLTN.Entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
Optional<UserEntity> findByUsername(String username);
    List<UserEntity> findAllByIsDeleteFalseAndStatusFalse();
    List<UserEntity> findAllByStatusTrueAndIsDeleteFalse();
    @Query("SELECT u FROM UserEntity u WHERE u.isDelete = false AND u.status = true")
    Page<UserEntity> findByIsDeletedFalseAndStatusTrue(Pageable pageable);
    @Query("SELECT u FROM UserEntity u WHERE (u.username LIKE %:searchTerm% OR u.firstname LIKE %:searchTerm% OR u.lastname LIKE %:searchTerm%) AND u.isDelete = false AND u.status = true")
    Page<UserEntity> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}