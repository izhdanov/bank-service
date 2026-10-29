package com.example.bank.service;

import com.example.bank.api.ApiException;
import com.example.bank.dao.entity.Account;
import com.example.bank.dao.repository.AccountRepository;
import com.example.bank.dto.TransferResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferService.class);
    private final AccountRepository accounts;

    public TransferService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Transactional
    public TransferResponse transfer(Long fromUserId, Long toUserId, BigDecimal value) {
        if (value == null || value.signum() <= 0 || value.scale() > 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must be positive with at most 2 decimals");
        }
        if (fromUserId.equals(toUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot transfer to yourself");
        }
        List<Account> locked = accounts.findAllByUserIdsForUpdate(Arrays.asList(fromUserId, toUserId));
        Account from = find(locked, fromUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Sender account not found"));
        Account to = find(locked, toUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Recipient account not found"));
        if (from.getBalance().compareTo(value) < 0) {
            log.warn("Transfer rejected: insufficient funds, from={}, to={}, value={}", fromUserId, toUserId, value);
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient funds");
        }
        from.setBalance(from.getBalance().subtract(value));
        to.setBalance(to.getBalance().add(value));
        log.info("Transfer done: from={}, to={}, value={}", fromUserId, toUserId, value);
        return new TransferResponse(fromUserId, toUserId, value, from.getBalance());
    }

    private Optional<Account> find(List<Account> list, Long userId) {
        return list.stream().filter(a -> a.getUser().getId().equals(userId)).findFirst();
    }
}
