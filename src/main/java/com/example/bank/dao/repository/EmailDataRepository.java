package com.example.bank.dao.repository;

import com.example.bank.dao.entity.EmailData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailDataRepository extends JpaRepository<EmailData, Long> {
    boolean existsByEmail(String email);

    long countByUserId(Long userId);

    Optional<EmailData> findByIdAndUserId(Long id, Long userId);
}
