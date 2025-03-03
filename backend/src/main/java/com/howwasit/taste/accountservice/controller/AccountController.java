package com.howwasit.taste.accountservice.controller;

import com.howwasit.taste.accountservice.data.dto.RequestAccount;
import com.howwasit.taste.accountservice.data.dto.ResponseAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.howwasit.taste.accountservice.service.AccountService;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/taste/account")
@RestController
public class AccountController {

    private final AccountService service;

    @PostMapping("/signup")
    public @ResponseBody ResponseAccount createAccount(@RequestBody RequestAccount requestDto) {
        return service.createAccount(requestDto);
    }

    @GetMapping("/info/{userId}")
    public @ResponseBody ResponseAccount getAccountInfo(@PathVariable String userId) {
        return service.getAccountInfo(userId);
    }

    @GetMapping("/info/all")
    public @ResponseBody List<ResponseAccount> getAllAccountInfo() {
        return service.getAllAccountInfo();
    }

//    @PutMapping("/update/id")
//    public @ResponseBody Integer updateAccountId(@RequestBody UpdateAccountIdVo vo) {
//        return service.updateAccountId(vo);
//    }
//
//    @PutMapping("/update/info")
//    public @ResponseBody void updateAccountInfo(@RequestBody AccountVo vo) {
//        service.updateAccountInfo(vo);
//    }
//
//    @DeleteMapping("/delete/{id}")
//    public @ResponseBody Integer deleteAccount(@PathVariable String id) {
//        return service.deleteAccount(id);
//    }
}
