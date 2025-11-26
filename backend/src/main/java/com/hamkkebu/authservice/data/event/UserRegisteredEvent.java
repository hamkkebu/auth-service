package com.hamkkebu.authservice.data.event;

import com.hamkkebu.boilerplate.data.event.BaseEvent;
import lombok.*;

/**
 * 사용자 회원가입 이벤트
 *
 * <p>Zero-Payload 패턴을 따라 userId(PK)만 포함합니다.</p>
 * <p>이벤트를 수신한 서비스는 gRPC를 통해 상세 정보를 조회합니다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class UserRegisteredEvent extends BaseEvent {

    /**
     * 이벤트 타입 상수
     */
    public static final String EVENT_TYPE = "USER_REGISTERED";

    /**
     * 회원가입한 사용자의 PK
     */
    private Long userPk;

    /**
     * DomainEvent 인터페이스 구현 - resourceId를 String으로 반환
     */
    @Override
    public String getResourceId() {
        return String.valueOf(userPk);
    }

    /**
     * Builder 패턴을 위한 생성자
     *
     * @param userPk 신규 등록된 사용자 ID
     */
    @Builder
    public UserRegisteredEvent(Long userPk) {
        super(EVENT_TYPE, String.valueOf(userPk), String.valueOf(userPk));
        this.userPk = userPk;
    }

    /**
     * 정적 팩토리 메소드
     *
     * @param userId 신규 등록된 사용자 ID
     * @return UserRegisteredEvent 인스턴스
     */
    public static UserRegisteredEvent of(Long userId) {
        return UserRegisteredEvent.builder()
                .userPk(userId)
                .build();
    }
}