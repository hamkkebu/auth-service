package com.hamkkebu.authservice.service;

import com.hamkkebu.authservice.data.entity.User;
import com.hamkkebu.authservice.repository.UserRepository;
import com.hamkkebu.boilerplate.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Keycloak 사용자 동기화 서비스 (JIT Provisioning)
 *
 * <p>Keycloak SSO로 로그인한 사용자를 auth-service DB에 자동 동기화합니다.</p>
 *
 * <p>JIT (Just-in-Time) Provisioning:</p>
 * <ul>
 *   <li>사용자가 처음 로그인할 때 DB에 자동 생성</li>
 *   <li>이미 존재하는 사용자는 정보 업데이트</li>
 *   <li>Keycloak의 역할(roles)을 auth-service 역할로 매핑</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserSyncService {

    private final UserRepository userRepository;

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";

    /**
     * Keycloak JWT에서 사용자 정보를 추출하여 DB에 동기화
     *
     * @param jwt Keycloak JWT 토큰
     * @return 동기화된 User 엔티티
     */
    @Transactional
    public User syncUser(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        log.debug("Keycloak 사용자 동기화 시작: keycloakUserId={}, username={}", keycloakUserId, username);

        // 1. Keycloak ID로 기존 사용자 조회
        Optional<User> existingUser = userRepository.findByKeycloakUserIdAndIsDeletedFalse(keycloakUserId);

        if (existingUser.isPresent()) {
            // 기존 사용자 - 로그인 시간 업데이트
            User user = existingUser.get();
            user.updateLastLoginAt();
            log.debug("기존 사용자 로그인: userId={}, username={}", user.getUserId(), user.getUsername());
            return userRepository.save(user);
        }

        // 2. username으로 기존 사용자 조회 (기존 회원이 Keycloak으로 처음 로그인하는 경우)
        Optional<User> userByUsername = userRepository.findByUsernameAndIsDeletedFalse(username);
        if (userByUsername.isPresent()) {
            // Keycloak ID 연결
            User user = userByUsername.get();
            // Reflection으로 keycloakUserId 설정 (setter가 없으므로)
            setKeycloakUserId(user, keycloakUserId);
            user.updateLastLoginAt();
            log.info("기존 사용자에 Keycloak 연결: userId={}, username={}, keycloakUserId={}",
                    user.getUserId(), user.getUsername(), keycloakUserId);
            return userRepository.save(user);
        }

        // 3. 새 사용자 생성 (JIT Provisioning)
        Role role = extractRole(jwt);

        User newUser = User.builder()
                .keycloakUserId(keycloakUserId)
                .username(username != null ? username : keycloakUserId)
                .email(email != null ? email : username + "@keycloak.local")
                .firstName(firstName)
                .lastName(lastName)
                .passwordHash("KEYCLOAK_MANAGED") // Keycloak이 비밀번호 관리
                .isActive(true)
                .isVerified(true) // Keycloak에서 이미 검증됨
                .role(role)
                .build();

        newUser.updateLastLoginAt();

        User savedUser = userRepository.save(newUser);
        log.info("새 사용자 JIT 프로비저닝: userId={}, username={}, keycloakUserId={}, role={}",
                savedUser.getUserId(), savedUser.getUsername(), keycloakUserId, role);

        return savedUser;
    }

    /**
     * Keycloak JWT에서 역할 추출
     */
    @SuppressWarnings("unchecked")
    private Role extractRole(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        if (realmAccess != null && realmAccess.containsKey(ROLES_CLAIM)) {
            List<String> roles = (List<String>) realmAccess.get(ROLES_CLAIM);

            // 가장 높은 권한 반환
            if (roles.contains("ADMIN")) {
                return Role.ADMIN;
            }
            if (roles.contains("DEVELOPER")) {
                return Role.DEVELOPER;
            }
        }
        return Role.USER;
    }

    /**
     * Reflection으로 keycloakUserId 설정
     */
    private void setKeycloakUserId(User user, String keycloakUserId) {
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("keycloakUserId");
            field.setAccessible(true);
            field.set(user, keycloakUserId);
        } catch (Exception e) {
            log.error("Failed to set keycloakUserId via reflection", e);
        }
    }
}
