package com.example.bank.service;

import com.example.bank.api.ApiException;
import com.example.bank.dao.entity.Account;
import com.example.bank.dao.entity.User;
import com.example.bank.dao.repository.AccountRepository;
import com.example.bank.dto.TransferResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {
    @Mock
    AccountRepository accounts;
    @InjectMocks
    TransferService service;

    private Account acc(long userId, String balance) {
        User u = new User();
        u.setId(userId);
        Account a = new Account();
        a.setUser(u);
        a.setBalance(new BigDecimal(balance));
        return a;
    }

    @Test
    void movesMoneyBetweenAccounts() {
        Account from = acc(1, "100.00"), to = acc(2, "5.50");
        when(accounts.findAllByUserIdsForUpdate(anyList())).thenReturn(Arrays.asList(from, to));
        TransferResponse r = service.transfer(1L, 2L, new BigDecimal("30.25"));
        assertEquals(new BigDecimal("69.75"), from.getBalance());
        assertEquals(new BigDecimal("35.75"), to.getBalance());
        assertEquals(new BigDecimal("69.75"), r.getBalance());
    }

    @Test
    void allowsTransferOfWholeBalance() {
        Account from = acc(1, "10.00"), to = acc(2, "0.00");
        when(accounts.findAllByUserIdsForUpdate(anyList())).thenReturn(Arrays.asList(from, to));
        service.transfer(1L, 2L, new BigDecimal("10.00"));
        assertEquals(0, from.getBalance().signum());
        assertEquals(new BigDecimal("10.00"), to.getBalance());
    }

    @Test
    void rejectsInsufficientFundsAndKeepsBalances() {
        Account from = acc(1, "10.00"), to = acc(2, "0.00");
        when(accounts.findAllByUserIdsForUpdate(anyList())).thenReturn(Arrays.asList(from, to));
        ApiException e = assertThrows(ApiException.class, () -> service.transfer(1L, 2L, new BigDecimal("10.01")));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, e.getStatus());
        assertEquals(new BigDecimal("10.00"), from.getBalance());
        assertEquals(new BigDecimal("0.00"), to.getBalance());
    }

    @Test
    void rejectsSelfTransferNonPositiveAndTooPreciseAmounts() {
        assertThrows(ApiException.class, () -> service.transfer(1L, 1L, BigDecimal.ONE));
        assertThrows(ApiException.class, () -> service.transfer(1L, 2L, new BigDecimal("-1")));
        assertThrows(ApiException.class, () -> service.transfer(1L, 2L, BigDecimal.ZERO));
        assertThrows(ApiException.class, () -> service.transfer(1L, 2L, new BigDecimal("1.001")));
        verifyNoInteractions(accounts);
    }

    @Test
    void failsWhenRecipientMissing() {
        when(accounts.findAllByUserIdsForUpdate(anyList())).thenReturn(Collections.singletonList(acc(1, "10.00")));
        ApiException e = assertThrows(ApiException.class, () -> service.transfer(1L, 2L, BigDecimal.ONE));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }
}
