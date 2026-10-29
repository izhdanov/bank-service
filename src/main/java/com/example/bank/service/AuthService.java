package com.example.bank.service;

import com.example.bank.api.ApiException;
import com.example.bank.dao.entity.User;
import com.example.bank.dao.repository.UserRepository;
import com.example.bank.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional(readOnly = true)
    public String login(String login, String password) {
        User user = (login.contains("@") ? users.findByEmails_Email(login) : users.findByPhones_Phone(login))
                .filter(u -> encoder.matches(password, u.getPassword()))
                .orElseThrow(() -> {
                    log.warn("Failed login attempt");
                    return new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });
        log.info("User {} authenticated", user.getId());
        return jwt.generate(user.getId());
    }
}
