package com.example.userservice.repository;

import com.example.userservice.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop5ByUserIdOrderByChangedAtDesc(Long userId);
}

