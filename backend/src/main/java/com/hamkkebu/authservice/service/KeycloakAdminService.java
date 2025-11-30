package com.hamkkebu.authservice.service;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Keycloak Admin API 서비스
 *
 * <p>Keycloak Admin REST API를 통해 사용자를 관리합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>사용자 삭제 (회원 탈퇴)</li>
 * </ul>
 */
@Slf4j
@Service
public class KeycloakAdminService {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.realm:master}")
    private String adminRealm;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String adminClientId;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    private Keycloak keycloak;

    @PostConstruct
    public void init() {
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(adminRealm)
                .clientId(adminClientId)
                .grantType(OAuth2Constants.PASSWORD)
                .username(adminUsername)
                .password(adminPassword)
                .build();

        log.info("Keycloak Admin Client 초기화 완료: serverUrl={}, adminRealm={}", authServerUrl, adminRealm);
    }

    /**
     * Keycloak에서 사용자 삭제
     *
     * @param keycloakUserId Keycloak 사용자 ID (sub claim)
     * @return 삭제 성공 여부
     */
    public boolean deleteUser(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            usersResource.delete(keycloakUserId);

            log.info("Keycloak 사용자 삭제 완료: keycloakUserId={}", keycloakUserId);
            return true;
        } catch (NotFoundException e) {
            log.warn("Keycloak 사용자를 찾을 수 없음: keycloakUserId={}", keycloakUserId);
            return false;
        } catch (Exception e) {
            log.error("Keycloak 사용자 삭제 실패: keycloakUserId={}, error={}", keycloakUserId, e.getMessage(), e);
            throw new RuntimeException("Keycloak 사용자 삭제 실패", e);
        }
    }

    /**
     * Keycloak 사용자 존재 여부 확인
     *
     * @param keycloakUserId Keycloak 사용자 ID
     * @return 존재 여부
     */
    public boolean userExists(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            usersResource.get(keycloakUserId).toRepresentation();
            return true;
        } catch (NotFoundException e) {
            return false;
        } catch (Exception e) {
            log.error("Keycloak 사용자 조회 실패: keycloakUserId={}, error={}", keycloakUserId, e.getMessage());
            return false;
        }
    }

    /**
     * Keycloak에 사용자 생성
     *
     * @param username  사용자 아이디
     * @param email     이메일
     * @param password  비밀번호
     * @param firstName 이름
     * @param lastName  성
     * @return 생성된 Keycloak 사용자 ID
     */
    public String createUser(String username, String email, String password, String firstName, String lastName) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // 사용자 정보 설정
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);

            // 비밀번호 설정
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            user.setCredentials(Collections.singletonList(credential));

            // 사용자 생성
            Response response = usersResource.create(user);

            if (response.getStatus() == 201) {
                // 생성된 사용자 ID 추출
                String locationHeader = response.getHeaderString("Location");
                String keycloakUserId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

                log.info("Keycloak 사용자 생성 완료: username={}, keycloakUserId={}", username, keycloakUserId);

                // 기본 역할 할당 (USER)
                assignRealmRole(keycloakUserId, "USER");

                return keycloakUserId;
            } else if (response.getStatus() == 409) {
                log.warn("Keycloak 사용자 이미 존재: username={}", username);
                throw new RuntimeException("이미 존재하는 사용자입니다");
            } else {
                String errorMessage = response.readEntity(String.class);
                log.error("Keycloak 사용자 생성 실패: username={}, status={}, error={}",
                        username, response.getStatus(), errorMessage);
                throw new RuntimeException("Keycloak 사용자 생성 실패: " + errorMessage);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Keycloak 사용자 생성 중 오류: username={}, error={}", username, e.getMessage(), e);
            throw new RuntimeException("Keycloak 사용자 생성 실패", e);
        }
    }

    /**
     * 사용자에게 Realm 역할 할당
     *
     * @param keycloakUserId Keycloak 사용자 ID
     * @param roleName       역할 이름
     */
    public void assignRealmRole(String keycloakUserId, String roleName) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);

            // 역할 조회
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();

            // 역할 할당
            userResource.roles().realmLevel().add(List.of(role));

            log.debug("Keycloak 역할 할당 완료: keycloakUserId={}, role={}", keycloakUserId, roleName);
        } catch (NotFoundException e) {
            log.warn("Keycloak 역할을 찾을 수 없음: roleName={}", roleName);
        } catch (Exception e) {
            log.error("Keycloak 역할 할당 실패: keycloakUserId={}, role={}, error={}",
                    keycloakUserId, roleName, e.getMessage());
        }
    }

    /**
     * username으로 Keycloak 사용자 존재 여부 확인
     *
     * @param username 사용자 아이디
     * @return 존재 여부
     */
    public boolean usernameExists(String username) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> users = usersResource.search(username, true);
            return !users.isEmpty();
        } catch (Exception e) {
            log.error("Keycloak 사용자 조회 실패: username={}, error={}", username, e.getMessage());
            return false;
        }
    }
}
