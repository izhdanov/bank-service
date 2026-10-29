package com.example.bank.api;

import com.example.bank.dto.*;
import com.example.bank.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public PageDto<UserDto> search(@RequestParam(required = false) String name,
                                   @RequestParam(required = false) String email,
                                   @RequestParam(required = false) String phone,
                                   @RequestParam(required = false)
                                   @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate dateOfBirth,
                                   @RequestParam(defaultValue = "0") @Min(0) int page,
                                   @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.search(new UserSearchCriteria(name, email, phone, dateOfBirth, page, size));
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/me/emails")
    @ResponseStatus(HttpStatus.CREATED)
    public void addEmail(@AuthenticationPrincipal Long userId, @Valid @RequestBody EmailRequest r) {
        service.addEmail(userId, r.getEmail());
    }

    @PutMapping("/me/emails/{id}")
    public void updateEmail(@AuthenticationPrincipal Long userId, @PathVariable Long id,
                            @Valid @RequestBody EmailRequest r) {
        service.updateEmail(userId, id, r.getEmail());
    }

    @DeleteMapping("/me/emails/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmail(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        service.deleteEmail(userId, id);
    }

    @PostMapping("/me/phones")
    @ResponseStatus(HttpStatus.CREATED)
    public void addPhone(@AuthenticationPrincipal Long userId, @Valid @RequestBody PhoneRequest r) {
        service.addPhone(userId, r.getPhone());
    }

    @PutMapping("/me/phones/{id}")
    public void updatePhone(@AuthenticationPrincipal Long userId, @PathVariable Long id,
                            @Valid @RequestBody PhoneRequest r) {
        service.updatePhone(userId, id, r.getPhone());
    }

    @DeleteMapping("/me/phones/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhone(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        service.deletePhone(userId, id);
    }
}
