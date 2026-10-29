package com.example.bank.service;

import com.example.bank.dao.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(name = "app.interest.enabled", havingValue = "true", matchIfMissing = true)
public class InterestScheduler {
    private static final Logger log = LoggerFactory.getLogger(InterestScheduler.class);
    private final AccountRepository accounts;
    private final BigDecimal multiplier;
    private final BigDecimal cap;

    public InterestScheduler(AccountRepository accounts,
                             @Value("${app.interest.rate}") BigDecimal rate,
                             @Value("${app.interest.max-multiplier}") BigDecimal cap) {
        this.accounts = accounts;
        this.multiplier = BigDecimal.ONE.add(rate);
        this.cap = cap;
    }

    @Scheduled(fixedRateString = "${app.interest.period-ms}")
    @Transactional
    public void accrue() {
        int updated = accounts.applyInterest(multiplier, cap);
        log.info("Interest accrued on {} accounts", updated);
    }
}
