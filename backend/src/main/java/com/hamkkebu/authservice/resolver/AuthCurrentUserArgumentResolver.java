package com.hamkkebu.authservice.resolver;

import com.hamkkebu.authservice.repository.UserRepository;
import com.hamkkebu.boilerplate.common.user.annotation.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Auth Service용 @CurrentUser 어노테이션을 처리하는 ArgumentResolver
 *
 * <p>SecurityContext에서 인증된 사용자의 ID를 추출하여 컨트롤러 파라미터로 주입합니다.</p>
 * <p>JWT 토큰의 subject에서 username을 추출하여 사용자 ID를 조회합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthCurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("No authentication found or not authenticated");
            return null;
        }

        String username = authentication.getName();

        if (username == null || username.equals("anonymousUser")) {
            log.debug("Anonymous user or null username");
            return null;
        }

        // JWT의 subject에는 username이 저장되어 있음
        // 먼저 숫자인지 확인 (userId가 직접 저장된 경우)
        try {
            return Long.parseLong(username);
        } catch (NumberFormatException e) {
            // username으로 사용자 조회
            log.debug("Looking up user by username: {}", username);
            return userRepository.findByUsernameAndIsDeletedFalse(username)
                    .map(user -> {
                        log.debug("Found user ID: {} for username: {}", user.getUserId(), username);
                        return user.getUserId();
                    })
                    .orElseGet(() -> {
                        log.warn("User not found for username: {}", username);
                        return null;
                    });
        }
    }
}
