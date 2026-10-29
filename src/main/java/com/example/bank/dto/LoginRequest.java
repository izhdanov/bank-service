package com.example.bank.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class LoginRequest {
    @NotBlank
    private String login;
    @NotBlank
    @Size(min = 8, max = 500)
    private String password;
}
