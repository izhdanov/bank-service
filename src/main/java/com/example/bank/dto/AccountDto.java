package com.example.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AccountDto {
    private Long userId;
    private BigDecimal balance;
}
