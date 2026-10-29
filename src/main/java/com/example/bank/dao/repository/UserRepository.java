package com.example.bank.dao.repository;

import com.example.bank.dao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmails_Email(String email);

    Optional<User> findByPhones_Phone(String phone);
}
