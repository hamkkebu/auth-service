package com.howwasit.taste.accountservice.repository;

import com.howwasit.taste.accountservice.data.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUserId(String userId);

//    Integer updateAccountId(UpdateAccountIdVo vo);
//    void updateAccountInfo(AccountVo vo);
//    void deleteById(String id);
}
