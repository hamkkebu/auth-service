package com.howwasit.taste.accountservice.service;

import java.util.List;

import com.howwasit.taste.accountservice.data.dto.RequestAccount;
import com.howwasit.taste.accountservice.data.dto.ResponseAccount;
import com.howwasit.taste.accountservice.data.entity.Account;
import com.howwasit.taste.accountservice.data.event.AccountEvent;
import com.howwasit.taste.accountservice.data.mapper.AccountMapper;
import com.howwasit.taste.accountservice.repository.AccountJpaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountMapper mapper;
    private final AccountJpaRepository repository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public ResponseAccount createAccount(RequestAccount requestDto) {

        Account entity = mapper.toEntity(requestDto);
        repository.save(entity);
        publisher.publishEvent(new AccountEvent(entity.getUserNum()));

        return mapper.toDto(entity);
    }

    public ResponseAccount getAccountInfo(String userId) {
        return mapper.toDto(repository.findByUserId(userId).orElseThrow());
    }

    public List<ResponseAccount> getAllAccountInfo() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

//    @Transactional
//    public Integer updateAccountId(UpdateAccountIdVo vo) {
//
//        Boolean flag = isExistAccount(vo.getTasteAccountOldId());
//        System.out.println(flag);
////        if (flag) {
////            return dao.updateAccountId(vo);
////        } else {
////            return new AccountVo();
////        }
//        return repository.updateAccountId(vo);
//    }
//
//    @Transactional
//    public void updateAccountInfo(AccountVo vo) {
//        dao.updateAccountInfo(vo);
//    }

//    @Transactional
//    public Integer deleteAccount(String id) {
//        Boolean flag = isExistAccount(id);
//        System.out.println(flag);
////        if (flag) {
////            return dao.deleteAccount(id);
////        } else {
////            return new AccountVo();
////        }
//        return dao.deleteAccount(id);
//    }

//    private boolean isExistAccount(String id) {
//
//        boolean flag = false;
//
//        // accountId 존재 여부 확인
//        AccountVo vo = getAccount(id);
//        if (vo != null) {
//            flag = true;
//        }
//
//        return flag;
//    }
}
