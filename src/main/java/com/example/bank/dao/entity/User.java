package com.example.bank.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 500)
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Account account;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private Set<EmailData> emails = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private Set<PhoneData> phones = new HashSet<>();

    public void addEmail(String email) {
        EmailData e = new EmailData();
        e.setUser(this);
        e.setEmail(email);
        emails.add(e);
    }

    public void addPhone(String phone) {
        PhoneData p = new PhoneData();
        p.setUser(this);
        p.setPhone(phone);
        phones.add(p);
    }

    public void attachAccount(BigDecimal initialBalance) {
        Account a = new Account();
        a.setUser(this);
        a.setBalance(initialBalance);
        a.setInitialBalance(initialBalance);
        this.account = a;
    }
}
