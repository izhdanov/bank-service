package com.example.bank.service;

import com.example.bank.api.ApiException;
import com.example.bank.dao.entity.EmailData;
import com.example.bank.dao.entity.PhoneData;
import com.example.bank.dao.entity.User;
import com.example.bank.dao.repository.EmailDataRepository;
import com.example.bank.dao.repository.PhoneDataRepository;
import com.example.bank.dao.repository.UserRepository;
import com.example.bank.dao.repository.UserSpecifications;
import com.example.bank.dto.PageDto;
import com.example.bank.dto.UserDto;
import com.example.bank.dto.UserSearchCriteria;
import com.example.bank.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository users;
    private final EmailDataRepository emails;
    private final PhoneDataRepository phones;
    private final UserMapper mapper;

    public UserService(UserRepository users, EmailDataRepository emails, PhoneDataRepository phones,
                       UserMapper mapper) {
        this.users = users;
        this.emails = emails;
        this.phones = phones;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "users", key = "#id")
    public UserDto getById(Long id) {
        return users.findById(id).map(mapper::toDto)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found: " + id));
    }

    @Transactional(readOnly = true)
    public PageDto<UserDto> search(UserSearchCriteria c) {
        Page<User> p = users.findAll(UserSpecifications.by(c), PageRequest.of(c.getPage(), c.getSize(), Sort.by("id")));
        return new PageDto<>(p.getContent().stream().map(mapper::toDto).collect(Collectors.toList()),
                p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    @Transactional
    @CacheEvict(cacheNames = "users", key = "#userId")
    public void addEmail(Long userId, String email) {
        if (emails.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already in use");
        }
        EmailData d = new EmailData();
        d.setUser(users.getReferenceById(userId));
        d.setEmail(email);
        emails.saveAndFlush(d);
        log.info("User {} added a email", userId);
    }

    @Transactional
    @CacheEvict(cacheNames = "users", key = "#userId")
    public void updateEmail(Long userId, Long id, String email) {
        EmailData d = emails.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Email not found"));
        if (d.getEmail().equals(email)) {
            return;
        }
        if (emails.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already in use");
        }
        d.setEmail(email);
        emails.flush();
        log.info("User {} changed email {}", userId, id);
    }

    @Transactional
    @CacheEvict(cacheNames = "users", key = "#userId")
    public void deleteEmail(Long userId, Long id) {
        EmailData d = emails.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Email not found"));
        if (emails.countByUserId(userId) <= 1) {
            throw new ApiException(HttpStatus.CONFLICT, "User must keep at least one email");
        }
        emails.delete(d);
        log.info("User {} deleted email {}", userId, id);
    }

    @Transactional
    @CacheEvict(cacheNames = "users", key = "#userId")
    public void addPhone(Long userId, String phone) {
        if (phones.existsByPhone(phone)) {
            throw new ApiException(HttpStatus.CONFLICT, "Phone is already in use");
        }
        PhoneData d = new PhoneData();
        d.setUser(users.getReferenceById(userId));
        d.setPhone(phone);
        phones.saveAndFlush(d);
        log.info("User {} added a phone", userId);
    }

    @Transactional
    @CacheEvict(cacheNames = "users", key = "#userId")
    public void updatePhone(Long userId, Long id, String phone) {
        PhoneData d = phones.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Phone not found"));
        if (d.getPhone().equals(phone)) {
            return;
        }
        if (phones.existsByPhone(phone)) {
            throw new ApiException(HttpStatus.CONFLICT, "Phone is already in use");
        }
        d.setPhone(phone);
        phones.flush();
        log.info("User {} changed phone {}", userId, id);
    }

    @Transactional
    @CacheEvict(cacheNames = "users", key = "#userId")
    public void deletePhone(Long userId, Long id) {
        PhoneData d = phones.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Phone not found"));
        if (phones.countByUserId(userId) <= 1) {
            throw new ApiException(HttpStatus.CONFLICT, "User must keep at least one phone");
        }
        phones.delete(d);
        log.info("User {} deleted phone {}", userId, id);
    }
}
