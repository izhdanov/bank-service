package com.example.bank.mapper;

import com.example.bank.dao.entity.EmailData;
import com.example.bank.dao.entity.PhoneData;
import com.example.bank.dao.entity.User;
import com.example.bank.dto.EmailDto;
import com.example.bank.dto.PhoneDto;
import com.example.bank.dto.UserDto;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getName(), u.getDateOfBirth(),
                u.getEmails().stream().sorted(Comparator.comparing(EmailData::getId))
                        .map(e -> new EmailDto(e.getId(), e.getEmail())).collect(Collectors.toList()),
                u.getPhones().stream().sorted(Comparator.comparing(PhoneData::getId))
                        .map(p -> new PhoneDto(p.getId(), p.getPhone())).collect(Collectors.toList()));
    }
}
