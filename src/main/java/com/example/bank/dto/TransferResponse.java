package com.example.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TransferResponse {
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal value;
    private BigDecimal balance;
}
