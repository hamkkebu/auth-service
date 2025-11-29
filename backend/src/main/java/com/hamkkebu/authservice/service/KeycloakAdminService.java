package com.hamkkebu.authservice.service;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
}
