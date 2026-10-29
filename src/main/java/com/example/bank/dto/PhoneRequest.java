package com.example.bank.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class PhoneRequest {
    @NotBlank
    @Pattern(regexp = "^7\\d{10}$", message = "must match format 79207865432")
    private String phone;
}
