package com.hamkkebu.authservice.service;

import com.hamkkebu.authservice.data.event.UserDeletedEvent;
import com.hamkkebu.authservice.data.event.UserRegisteredEvent;
import com.hamkkebu.boilerplate.common.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 사용자 이벤트 비동기 발행기
 *
 * <p>@Async를 사용하여 Kafka 이벤트를 비동기로 발행합니다.</p>
 * <p>Kafka 연결 실패 시에도 주 트랜잭션에 영향을 주지 않습니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final EventPublisher eventPublisher;

    @Value("${kafka.topics.user-events:user.events}")
    private String userEventsTopic;

    /**
     * 회원가입 이벤트 비동기 발행
     *
     * @param userId 사용자 ID
     */
    @Async("eventExecutor")
    public void publishUserRegisteredEvent(Long userId) {
        try {
            UserRegisteredEvent event = UserRegisteredEvent.of(userId);
            eventPublisher.publish(userEventsTopic, event);
            log.info("회원가입 이벤트 발행 완료: userId={}, eventId={}", userId, event.getEventId());
        } catch (Exception e) {
            log.warn("회원가입 이벤트 발행 실패 (Kafka 연결 불가): userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 회원탈퇴 이벤트 비동기 발행
     *
     * @param userId 사용자 ID
     */
    @Async("eventExecutor")
    public void publishUserDeletedEvent(Long userId) {
        try {
            UserDeletedEvent event = UserDeletedEvent.of(userId);
            eventPublisher.publish(userEventsTopic, event);
            log.info("회원탈퇴 이벤트 발행 완료: userId={}, eventId={}", userId, event.getEventId());
        } catch (Exception e) {
            log.warn("회원탈퇴 이벤트 발행 실패 (Kafka 연결 불가): userId={}, error={}", userId, e.getMessage());
        }
    }
}
