package com.example.bank.api;

import com.example.bank.dao.entity.User;
import com.example.bank.dao.repository.AccountRepository;
import com.example.bank.dao.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.interest.enabled=false")
@AutoConfigureMockMvc
@Testcontainers
class BankApiIT {
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
    }

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired AccountRepository accounts;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper mapper;

    private User newUser(String name, String balance) {
        User u = new User();
        u.setName(name);
        u.setDateOfBirth(LocalDate.of(1993, 5, 1));
        u.setPassword(encoder.encode("password123"));
        u.addEmail(UUID.randomUUID().toString().substring(0, 12) + "@test.com");
        u.addPhone("7" + (1_000_000_000L + ThreadLocalRandom.current().nextLong(8_999_999_999L)));
        u.attachAccount(new BigDecimal(balance));
        return users.save(u);
    }

    private String token(User u) throws Exception {
        String body = mapper.writeValueAsString(Collections.singletonMap("login", u.getEmails().iterator().next().getEmail()))
                .replace("}", ",\"password\":\"password123\"}");
        String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(json).get("token").asText();
    }

    private BigDecimal balance(User u) {
        return accounts.findByUserId(u.getId()).get().getBalance();
    }

    @Test
    void searchByNamePrefixWithPagination() throws Exception {
        String prefix = "Zed" + UUID.randomUUID().toString().substring(0, 6);
        User a = newUser(prefix + " One", "10.00");
        newUser(prefix + " Two", "10.00");
        mvc.perform(get("/api/users").param("name", prefix).param("size", "1")
                        .header("Authorization", token(a)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void transferOverHttpUpdatesBalances() throws Exception {
        User a = newUser("A", "100.00"), b = newUser("B", "0.00");
        String ta = token(a);
        mvc.perform(post("/api/transfers").header("Authorization", ta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUserId\":" + b.getId() + ",\"value\":40.50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(59.50));
        assertEquals(new BigDecimal("59.50"), balance(a));
        assertEquals(new BigDecimal("40.50"), balance(b));
        mvc.perform(post("/api/transfers").header("Authorization", ta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUserId\":" + b.getId() + ",\"value\":1000}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }

    @Test
    void concurrentTransfersNeverOverdraw() throws Exception {
        User a = newUser("A", "10.00"), b = newUser("B", "10.00");
        String ta = token(a), tb = token(b);
        ExecutorService pool = Executors.newFixedThreadPool(16);
        List<Future<?>> fs = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            final boolean fromA = i % 2 == 0;
            fs.add(pool.submit(() -> {
                try {
                    mvc.perform(post("/api/transfers")
                            .header("Authorization", fromA ? ta : tb)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"toUserId\":" + (fromA ? b : a).getId() + ",\"value\":3.00}"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        for (Future<?> f : fs) f.get(60, TimeUnit.SECONDS);
        pool.shutdown();
        assertEquals(new BigDecimal("20.00"), balance(a).add(balance(b)));
        assertTrue(balance(a).signum() >= 0);
        assertTrue(balance(b).signum() >= 0);
    }
}
