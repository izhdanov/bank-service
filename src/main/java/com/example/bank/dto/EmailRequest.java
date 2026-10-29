package com.example.bank.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class EmailRequest {
    @NotBlank
    @Email
    @Size(max = 200)
    private String email;
}
