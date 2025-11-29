package com.hamkkebu.authservice.config;

import com.hamkkebu.authservice.service.KeycloakUserSyncService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Keycloak JIT (Just-in-Time) Provisioning 필터
 *
 * <p>JWT 인증 후 사용자 정보를 auth-service DB에 동기화합니다.</p>
 *
 * <p>동작 방식:</p>
 * <ol>
 *   <li>Spring Security JWT 인증 완료 후 실행</li>
 *   <li>인증된 사용자의 JWT 토큰에서 정보 추출</li>
 *   <li>auth-service DB에 사용자 정보 동기화</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakJitProvisioningFilter extends OncePerRequestFilter {

    private final KeycloakUserSyncService keycloakUserSyncService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();

                // JIT Provisioning - 사용자 DB 동기화
                keycloakUserSyncService.syncUser(jwt);
            }
        } catch (Exception e) {
            // JIT 프로비저닝 실패해도 요청 처리는 계속
            log.warn("JIT Provisioning failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 인증이 필요 없는 엔드포인트는 필터 스킵
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 인증 없이 접근 가능한 엔드포인트는 스킵
        return path.startsWith("/api/v1/users/check") ||
               path.startsWith("/api/v1/samples/check") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/api/v1/users") && "POST".equals(request.getMethod()) ||
               path.equals("/api/v1/samples") && "POST".equals(request.getMethod());
    }
}
