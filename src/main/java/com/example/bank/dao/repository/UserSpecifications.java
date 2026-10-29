package com.example.bank.dao.repository;

import com.example.bank.dao.entity.User;
import com.example.bank.dto.UserSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public final class UserSpecifications {
    private UserSpecifications() {
    }

    public static Specification<User> by(UserSearchCriteria c) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> p = new ArrayList<>();
            if (c.getDateOfBirth() != null) {
                p.add(cb.greaterThan(root.get("dateOfBirth"), c.getDateOfBirth()));
            }
            if (c.getName() != null && !c.getName().isEmpty()) {
                String escaped = c.getName().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                p.add(cb.like(root.get("name"), escaped + "%", '\\'));
            }
            if (c.getEmail() != null && !c.getEmail().isEmpty()) {
                p.add(cb.equal(root.join("emails").get("email"), c.getEmail()));
            }
            if (c.getPhone() != null && !c.getPhone().isEmpty()) {
                p.add(cb.equal(root.join("phones").get("phone"), c.getPhone()));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
