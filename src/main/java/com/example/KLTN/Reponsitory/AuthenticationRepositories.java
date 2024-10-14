package com.example.KLTN.Reponsitory;


import com.example.KLTN.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AuthenticationRepositories extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);
}
