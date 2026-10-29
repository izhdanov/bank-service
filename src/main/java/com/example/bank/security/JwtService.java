package com.example.bank.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    private final Key key;
    private final long ttlMillis;

    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.ttl-minutes}") long ttlMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMillis = ttlMinutes * 60_000;
    }

    public String generate(Long userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder().claim("USER_ID", userId)
                .setIssuedAt(new Date(now)).setExpiration(new Date(now + ttlMillis))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public Long parse(String token) {
        Claims c = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        Number n = c.get("USER_ID", Number.class);
        return n == null ? null : n.longValue();
    }
}
