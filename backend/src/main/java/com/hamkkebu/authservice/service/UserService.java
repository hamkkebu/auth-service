package com.hamkkebu.authservice.service;

import com.hamkkebu.authservice.data.dto.DuplicateCheckResponse;
import com.hamkkebu.authservice.data.dto.UserRequest;
import com.hamkkebu.authservice.data.dto.UserResponse;
import com.hamkkebu.authservice.data.entity.User;
import com.hamkkebu.authservice.data.event.UserRegisteredEvent;
import com.hamkkebu.authservice.data.mapper.UserMapper;
import com.hamkkebu.authservice.repository.UserRepository;
import com.hamkkebu.boilerplate.common.enums.Role;
import com.hamkkebu.boilerplate.common.exception.BusinessException;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import com.hamkkebu.boilerplate.common.publisher.EventPublisher;
import com.hamkkebu.boilerplate.common.security.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * User 서비스
 *
 * <p>사용자 등록, 조회, 중복 확인 등의 비즈니스 로직을 처리합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordValidator passwordValidator;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;
    private final KeycloakAdminService keycloakAdminService;

    @Value("${kafka.topics.user-events:user.events}")
    private String userEventsTopic;

    /**
     * 사용자 등록 (회원가입)
     *
     * @param request 사용자 등록 요청
     * @return 등록된 사용자 정보
     */
    @Transactional
    public UserResponse registerUser(UserRequest request) {
        log.info("사용자 등록 시작: username={}", request.getUsername());

        // 아이디 중복 확인 (탈퇴한 회원 포함)
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("중복된 아이디 (탈퇴 회원 포함): {}", request.getUsername());
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용된 아이디입니다 (탈퇴한 회원 포함)");
        }

        // 이메일 중복 확인 (탈퇴한 회원 포함)
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("중복된 이메일 (탈퇴 회원 포함): {}", request.getEmail());
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용된 이메일입니다 (탈퇴한 회원 포함)");
        }

        // SECURITY: 비밀번호 강도 검증
        passwordValidator.validatePasswordFormat(request.getPassword());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 엔티티 생성
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(encodedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .country(request.getCountry())
                .city(request.getCity())
                .state(request.getState())
                .streetAddress(request.getStreetAddress())
                .postalCode(request.getPostalCode())
                .isActive(true)
                .isVerified(false)
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("사용자 등록 완료: userId={}, username={}", savedUser.getUserId(), savedUser.getUsername());

        // 회원가입 이벤트 발행 (Kafka 연결 실패 시 무시, 비동기 처리)
        publishUserRegisteredEventAsync(savedUser.getUserId());

        return userMapper.toDto(savedUser);
    }

    /**
     * 사용자 아이디 중복 확인 (탈퇴한 회원 포함)
     *
     * @param username 확인할 아이디
     * @return 중복 확인 결과
     */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkUsernameDuplicate(String username) {
        boolean exists = userRepository.existsByUsername(username);
        log.debug("아이디 중복 확인 (탈퇴 회원 포함): username={}, exists={}", username, exists);
        return DuplicateCheckResponse.of(exists, username);
    }

    /**
     * 사용자 이메일 중복 확인 (탈퇴한 회원 포함)
     *
     * @param email 확인할 이메일
     * @return 중복 확인 결과
     */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkEmailDuplicate(String email) {
        boolean exists = userRepository.existsByEmail(email);
        log.debug("이메일 중복 확인 (탈퇴 회원 포함): email={}, exists={}", email, exists);
        return DuplicateCheckResponse.of(exists, email);
    }

    /**
     * 사용자 조회 by userId
     *
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toDto(user);
    }

    /**
     * 사용자 조회 by username
     *
     * @param username 사용자 아이디
     * @return 사용자 정보
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toDto(user);
    }

    /**
     * 사용자 삭제 (회원 탈퇴) by username
     *
     * <p>Keycloak 사용자와 일반 사용자를 구분하여 처리합니다:</p>
     * <ul>
     *   <li>Keycloak 사용자: Keycloak에서 삭제 후 DB soft delete (비밀번호 검증 불필요)</li>
     *   <li>일반 사용자: 비밀번호 확인 후 DB soft delete</li>
     * </ul>
     *
     * @param username 사용자 아이디
     * @param password 비밀번호 (일반 사용자만 필요, Keycloak 사용자는 null 가능)
     */
    @Transactional
    public void deleteUserByUsername(String username, String password) {
        log.info("사용자 탈퇴 시작: username={}", username);

        // 사용자 조회
        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Keycloak 사용자인지 확인
        boolean isKeycloakUser = StringUtils.hasText(user.getKeycloakUserId());

        if (isKeycloakUser) {
            // Keycloak 사용자: Keycloak에서 삭제
            log.info("Keycloak 사용자 탈퇴 처리: username={}, keycloakUserId={}", username, user.getKeycloakUserId());

            try {
                keycloakAdminService.deleteUser(user.getKeycloakUserId());
                log.info("Keycloak에서 사용자 삭제 완료: keycloakUserId={}", user.getKeycloakUserId());
            } catch (Exception e) {
                log.error("Keycloak 사용자 삭제 실패: keycloakUserId={}, error={}", user.getKeycloakUserId(), e.getMessage());
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Keycloak 사용자 삭제에 실패했습니다");
            }
        } else {
            // 일반 사용자: 비밀번호 확인 필요
            if (!StringUtils.hasText(password)) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "비밀번호를 입력해주세요");
            }

            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                log.warn("비밀번호 불일치: username={}", username);
                throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "비밀번호가 일치하지 않습니다");
            }
        }

        // DB Soft Delete
        user.delete();
        userRepository.save(user);
        log.info("사용자 탈퇴 완료: username={}, isKeycloakUser={}", username, isKeycloakUser);
    }

    /**
     * 사용자가 Keycloak 사용자인지 확인
     *
     * @param username 사용자 아이디
     * @return Keycloak 사용자 여부
     */
    @Transactional(readOnly = true)
    public boolean isKeycloakUser(String username) {
        return userRepository.findByUsernameAndIsDeletedFalse(username)
                .map(user -> StringUtils.hasText(user.getKeycloakUserId()))
                .orElse(false);
    }

    /**
     * 회원가입 이벤트 비동기 발행 (Kafka 연결 실패 시 무시)
     */
    private void publishUserRegisteredEventAsync(Long userId) {
        new Thread(() -> {
            try {
                UserRegisteredEvent event = UserRegisteredEvent.of(userId);
                eventPublisher.publish(userEventsTopic, event);
                log.info("회원가입 이벤트 발행 완료: userId={}, eventId={}", userId, event.getEventId());
            } catch (Exception e) {
                log.warn("회원가입 이벤트 발행 실패 (Kafka 연결 불가): userId={}, error={}", userId, e.getMessage());
            }
        }).start();
    }

}
