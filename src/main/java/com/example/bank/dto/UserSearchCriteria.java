package com.example.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class UserSearchCriteria {
    private final String name;
    private final String email;
    private final String phone;
    private final LocalDate dateOfBirth;
    private final int page;
    private final int size;
}
