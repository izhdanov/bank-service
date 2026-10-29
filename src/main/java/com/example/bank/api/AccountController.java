package com.example.bank.api;

import com.example.bank.dto.AccountDto;
import com.example.bank.dto.TransferRequest;
import com.example.bank.dto.TransferResponse;
import com.example.bank.service.AccountService;
import com.example.bank.service.TransferService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class AccountController {
    private final TransferService transfers;
    private final AccountService accounts;

    public AccountController(TransferService transfers, AccountService accounts) {
        this.transfers = transfers;
        this.accounts = accounts;
    }

    @GetMapping("/accounts/me")
    public AccountDto myAccount(@AuthenticationPrincipal Long userId) {
        return accounts.getByUserId(userId);
    }

    @PostMapping("/transfers")
    public TransferResponse transfer(@AuthenticationPrincipal Long userId, @Valid @RequestBody TransferRequest r) {
        return transfers.transfer(userId, r.getToUserId(), r.getValue());
    }
}
