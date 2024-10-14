package com.example.KLTN.Reponsitory;

import com.example.KLTN.Entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedReponsitory extends JpaRepository<InvalidatedToken, String> {
}
