package com.example.bank.dao.repository;

import com.example.bank.dao.entity.PhoneData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {
    boolean existsByPhone(String phone);

    long countByUserId(Long userId);

    Optional<PhoneData> findByIdAndUserId(Long id, Long userId);
}
