package com.howwasit.taste.accountservice.listener;

import com.howwasit.taste.accountservice.data.event.AccountEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountListener {

    @Async
    @EventListener
    public void getEvent(AccountEvent event) {
        log.info("[Account Service] Event 수신 완료");
    }
}
