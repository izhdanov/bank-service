package com.example.bank.dao.repository;

import com.example.bank.dao.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.user.id in :userIds order by a.id")
    List<Account> findAllByUserIdsForUpdate(@Param("userIds") List<Long> userIds);

    @Modifying
    @Query(value = "update account set balance = least(round(balance * :multiplier, 2), round(initial_balance * :cap, 2)) "
            + "where balance < round(initial_balance * :cap, 2)", nativeQuery = true)
    int applyInterest(@Param("multiplier") BigDecimal multiplier, @Param("cap") BigDecimal cap);
}
