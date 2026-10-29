package com.example.bank.service;

import com.example.bank.api.ApiException;
import com.example.bank.dao.repository.AccountRepository;
import com.example.bank.dto.AccountDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accounts;

    public AccountService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public AccountDto getByUserId(Long userId) {
        return accounts.findByUserId(userId).map(a -> new AccountDto(userId, a.getBalance()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found"));
    }
}
