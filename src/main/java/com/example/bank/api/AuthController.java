package com.example.bank.api;

import com.example.bank.dto.LoginRequest;
import com.example.bank.dto.TokenResponse;
import com.example.bank.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest r) {
        return new TokenResponse(auth.login(r.getLogin(), r.getPassword()));
    }
}
