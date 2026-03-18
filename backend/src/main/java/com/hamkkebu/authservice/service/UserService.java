package com.hamkkebu.authservice.service;

import com.hamkkebu.authservice.data.dto.DuplicateCheckResponse;
import com.hamkkebu.authservice.data.dto.PasswordChangeRequest;
import com.hamkkebu.authservice.data.dto.ProfileUpdateRequest;
import com.hamkkebu.authservice.data.dto.UserRequest;
import com.hamkkebu.authservice.data.dto.UserResponse;
import com.hamkkebu.authservice.data.entity.User;
import com.hamkkebu.authservice.data.mapper.UserMapper;
import com.hamkkebu.authservice.repository.UserRepository;
import com.hamkkebu.boilerplate.common.enums.Role;
import com.hamkkebu.boilerplate.common.exception.BusinessException;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import com.hamkkebu.boilerplate.common.security.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserEventPublisher userEventPublisher;
    private final KeycloakAdminService keycloakAdminService;

    /**
     * 사용자 등록 (회원가입)
     *
     * <p>Keycloak과 로컬 DB 모두에 사용자를 생성합니다.</p>
     *
     * @param request 사용자 등록 요청
     * @return 등록된 사용자 정보
     */
    @Transactional
    public UserResponse registerUser(UserRequest request) {
        log.info("사용자 등록 시작: username={}", request.getUsername());

        // Keycloak 중복 확인 (Keycloak에서 unique 제약 관리)
        if (keycloakAdminService.usernameExists(request.getUsername())) {
            log.warn("Keycloak에 이미 존재하는 아이디: {}", request.getUsername());
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용된 아이디입니다");
        }

        // SECURITY: 비밀번호 강도 검증
        passwordValidator.validatePasswordFormat(request.getPassword());

        // 1. Keycloak에 사용자 생성
        String keycloakUserId;
        try {
            keycloakUserId = keycloakAdminService.createUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName()
            );
        } catch (Exception e) {
            log.error("Keycloak 사용자 생성 실패: username={}, error={}", request.getUsername(), e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "회원가입 처리 중 오류가 발생했습니다");
        }

        // 2. 비밀번호 암호화 (로컬 DB 저장용)
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. User 엔티티 생성 (Keycloak ID 포함)
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
                .isVerified(true)  // Keycloak에서 이메일 검증됨으로 설정
                .role(Role.USER)
                .keycloakUserId(keycloakUserId)  // Keycloak 사용자 ID 저장
                .build();

        User savedUser = userRepository.save(user);
        log.info("사용자 등록 완료: userId={}, username={}, keycloakUserId={}",
                savedUser.getUserId(), savedUser.getUsername(), keycloakUserId);

        // 회원가입 이벤트 발행 (Kafka 연결 실패 시 무시, 비동기 처리)
        userEventPublisher.publishUserRegisteredEvent(savedUser.getUserId());

        return userMapper.toDto(savedUser);
    }

    /**
     * 사용자 아이디 중복 확인 (로컬 DB + Keycloak)
     *
     * <p>로컬 DB와 Keycloak 모두에서 아이디 중복을 확인합니다.</p>
     *
     * @param username 확인할 아이디
     * @return 중복 확인 결과
     */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkUsernameDuplicate(String username) {
        // 로컬 DB 확인 (탈퇴한 회원 포함)
        boolean existsInDb = userRepository.existsByUsername(username);

        // Keycloak 확인
        boolean existsInKeycloak = false;
        try {
            existsInKeycloak = keycloakAdminService.usernameExists(username);
        } catch (Exception e) {
            log.warn("Keycloak 중복 확인 실패 (DB만 확인): username={}, error={}", username, e.getMessage());
        }

        boolean exists = existsInDb || existsInKeycloak;
        log.debug("아이디 중복 확인: username={}, existsInDb={}, existsInKeycloak={}, exists={}",
                username, existsInDb, existsInKeycloak, exists);

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

        // 회원탈퇴 이벤트 발행 (Kafka 연결 실패 시 무시, 비동기 처리)
        userEventPublisher.publishUserDeletedEvent(user.getUserId());
    }

    /**
     * 사용자 프로필 업데이트
     *
     * <p>Keycloak과 로컬 DB 모두에 사용자 정보를 업데이트합니다.</p>
     * <p>Keycloak에는 email, firstName, lastName만 동기화하고,
     * 나머지 프로필 정보는 로컬 DB에만 저장합니다.</p>
     *
     * @param userId  사용자 ID
     * @param request 프로필 수정 요청
     * @return 수정된 사용자 정보
     */
    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        log.info("사용자 프로필 수정 시작: userId={}", userId);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Keycloak 사용자인 경우 Keycloak 정보도 업데이트
        if (StringUtils.hasText(user.getKeycloakUserId())) {
            try {
                keycloakAdminService.updateUser(
                        user.getKeycloakUserId(),
                        request.getEmail(),
                        request.getFirstName(),
                        request.getLastName()
                );
            } catch (Exception e) {
                log.error("Keycloak 사용자 업데이트 실패: keycloakUserId={}, error={}",
                        user.getKeycloakUserId(), e.getMessage());
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "프로필 수정 중 오류가 발생했습니다");
            }
        }

        // 로컬 DB 업데이트
        userMapper.updateProfileFromRequest(request, user);
        User savedUser = userRepository.save(user);

        log.info("사용자 프로필 수정 완료: userId={}", savedUser.getUserId());

        // 프로필 수정 이벤트 발행 (다른 서비스에 동기화)
        userEventPublisher.publishUserUpdatedEvent(savedUser.getUserId());

        return userMapper.toDto(savedUser);
    }

    /**
     * 비밀번호 변경
     *
     * <p>현재 비밀번호를 확인한 후, Keycloak과 로컬 DB 모두에 새 비밀번호를 반영합니다.</p>
     *
     * @param userId  사용자 ID
     * @param request 비밀번호 변경 요청
     */
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        log.info("비밀번호 변경 시작: userId={}", userId);

        // 새 비밀번호 확인 일치 검증
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "새 비밀번호가 일치하지 않습니다");
        }

        // 새 비밀번호 형식 검증
        passwordValidator.validatePasswordFormat(request.getNewPassword());

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            log.warn("현재 비밀번호 불일치: userId={}", userId);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "현재 비밀번호가 일치하지 않습니다");
        }

        // 현재 비밀번호와 새 비밀번호가 같은지 확인
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다");
        }

        // Keycloak 사용자인 경우 Keycloak 비밀번호도 변경
        if (StringUtils.hasText(user.getKeycloakUserId())) {
            try {
                keycloakAdminService.resetPassword(user.getKeycloakUserId(), request.getNewPassword());
            } catch (Exception e) {
                log.error("Keycloak 비밀번호 변경 실패: keycloakUserId={}, error={}",
                        user.getKeycloakUserId(), e.getMessage());
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "비밀번호 변경 중 오류가 발생했습니다");
            }
        }

        // 로컬 DB 비밀번호 업데이트
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedNewPassword);
        userRepository.save(user);

        log.info("비밀번호 변경 완료: userId={}", userId);
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
}
