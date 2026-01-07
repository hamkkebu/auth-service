package com.hamkkebu.authservice.controller;

import com.hamkkebu.authservice.data.dto.DeleteUserRequest;
import com.hamkkebu.authservice.data.dto.DuplicateCheckResponse;
import com.hamkkebu.authservice.data.dto.UserRequest;
import com.hamkkebu.authservice.data.dto.UserResponse;
import com.hamkkebu.authservice.service.UserService;
import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import com.hamkkebu.boilerplate.common.exception.BusinessException;
import com.hamkkebu.boilerplate.common.exception.ErrorCode;
import com.hamkkebu.boilerplate.common.user.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User API 컨트롤러
 *
 * <p>사용자 등록, 조회, 중복 확인 등의 API를 제공합니다.</p>
 */
@Tag(name = "User API", description = "사용자 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 등록 (회원가입)
     *
     * <p>새로운 사용자를 등록합니다.</p>
     * <p>아이디와 이메일 중복을 확인하고, 비밀번호를 암호화하여 저장합니다.</p>
     *
     * @param request 사용자 등록 요청
     * @return 등록된 사용자 정보
     */
    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 등록합니다. 아이디와 이메일 중복을 확인합니다.",
        security = {} // 인증 불필요
    )
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRequest request) {
        log.info("회원가입 요청: username={}", request.getUsername());
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "회원가입이 완료되었습니다"));
    }

    /**
     * 아이디 중복 확인
     *
     * @param username 확인할 아이디
     * @return 중복 확인 결과
     */
    @Operation(
        summary = "아이디 중복 확인",
        description = "사용자 아이디의 중복 여부를 확인합니다.",
        security = {} // 인증 불필요
    )
    @GetMapping("/check/{username}")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkUsernameDuplicate(@PathVariable String username) {
        log.debug("아이디 중복 확인: username={}", username);
        DuplicateCheckResponse response = userService.checkUsernameDuplicate(username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 닉네임 중복 확인 (username과 동일하게 처리)
     *
     * <p>현재는 nickname 필드가 없으므로 username 중복 확인으로 처리합니다.</p>
     *
     * @param nickname 확인할 닉네임
     * @return 중복 확인 결과
     */
    @Operation(
        summary = "닉네임 중복 확인",
        description = "닉네임의 중복 여부를 확인합니다. (현재는 username과 동일하게 처리)",
        security = {} // 인증 불필요
    )
    @GetMapping("/check/nickname/{nickname}")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkNicknameDuplicate(@PathVariable String nickname) {
        log.debug("닉네임 중복 확인: nickname={}", nickname);
        // 현재는 nickname 필드가 없으므로 username 중복 확인으로 처리
        DuplicateCheckResponse response = userService.checkUsernameDuplicate(nickname);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 이메일 중복 확인
     *
     * @param email 확인할 이메일
     * @return 중복 확인 결과
     */
    @Operation(
        summary = "이메일 중복 확인",
        description = "이메일의 중복 여부를 확인합니다.",
        security = {} // 인증 불필요
    )
    @GetMapping("/check/email/{email}")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkEmailDuplicate(@PathVariable String email) {
        log.debug("이메일 중복 확인: email={}", email);
        DuplicateCheckResponse response = userService.checkEmailDuplicate(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 정보 조회 (로그인한 사용자 본인)
     *
     * @param currentUserId 현재 로그인한 사용자 ID
     * @return 사용자 정보
     */
    @Operation(
        summary = "내 정보 조회",
        description = "현재 로그인한 사용자의 정보를 조회합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(
            @Parameter(hidden = true) @CurrentUser Long currentUserId) {
        log.debug("내 정보 조회: userId={}", currentUserId);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }
        UserResponse response = userService.getUserById(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 사용자 조회 by userId
     *
     * <p>보안: 본인만 조회 가능합니다.</p>
     *
     * @param currentUserId 현재 로그인한 사용자 ID
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    @Operation(
        summary = "사용자 조회 (ID)",
        description = "사용자 ID로 사용자 정보를 조회합니다. 본인만 조회 가능합니다."
    )
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(hidden = true) @CurrentUser Long currentUserId,
            @PathVariable Long userId) {
        log.debug("사용자 조회: userId={}, currentUserId={}", userId, currentUserId);

        // 본인만 조회 가능
        if (currentUserId == null || !currentUserId.equals(userId)) {
            log.warn("사용자 {}가 다른 사용자 {}의 정보를 조회 시도", currentUserId, userId);
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 사용자 조회 by username
     *
     * <p>보안: 본인만 조회 가능합니다.</p>
     *
     * @param currentUserId 현재 로그인한 사용자 ID
     * @param username 사용자 아이디
     * @return 사용자 정보
     */
    @Operation(
        summary = "사용자 조회 (username)",
        description = "사용자 아이디로 사용자 정보를 조회합니다. 본인만 조회 가능합니다."
    )
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(
            @Parameter(hidden = true) @CurrentUser Long currentUserId,
            @PathVariable String username) {
        log.debug("사용자 조회: username={}, currentUserId={}", username, currentUserId);

        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        // 조회하려는 사용자가 본인인지 확인
        UserResponse response = userService.getUserByUsername(username);
        if (!currentUserId.equals(response.getUserId())) {
            log.warn("사용자 {}가 다른 사용자 {}의 정보를 조회 시도", currentUserId, username);
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Keycloak 사용자 여부 확인
     *
     * <p>보안: 본인만 확인 가능합니다.</p>
     *
     * @param currentUserId 현재 로그인한 사용자 ID
     * @param username 사용자 아이디
     * @return Keycloak 사용자 여부
     */
    @Operation(
        summary = "Keycloak 사용자 여부 확인",
        description = "사용자가 Keycloak SSO 사용자인지 확인합니다. 본인만 확인 가능합니다."
    )
    @GetMapping("/username/{username}/keycloak")
    public ResponseEntity<ApiResponse<Boolean>> isKeycloakUser(
            @Parameter(hidden = true) @CurrentUser Long currentUserId,
            @PathVariable String username) {
        log.debug("Keycloak 사용자 확인: username={}, currentUserId={}", username, currentUserId);

        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        // 조회하려는 사용자가 본인인지 확인
        UserResponse user = userService.getUserByUsername(username);
        if (!currentUserId.equals(user.getUserId())) {
            log.warn("사용자 {}가 다른 사용자 {}의 Keycloak 정보를 조회 시도", currentUserId, username);
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        boolean isKeycloak = userService.isKeycloakUser(username);
        return ResponseEntity.ok(ApiResponse.success(isKeycloak));
    }

    /**
     * 사용자 삭제 (회원 탈퇴) by username
     *
     * <p>Keycloak 사용자는 비밀번호 없이 탈퇴 가능합니다.</p>
     * <p>일반 사용자는 비밀번호 확인이 필요합니다.</p>
     * <p>보안: 본인만 탈퇴 가능합니다.</p>
     *
     * @param currentUserId 현재 로그인한 사용자 ID
     * @param username 사용자 아이디
     * @param request 삭제 요청 (비밀번호 포함, Keycloak 사용자는 선택)
     * @return 성공 메시지
     */
    @Operation(
        summary = "회원 탈퇴",
        description = "회원을 탈퇴합니다. Keycloak 사용자는 비밀번호 없이, 일반 사용자는 비밀번호 확인이 필요합니다. 본인만 탈퇴 가능합니다."
    )
    @DeleteMapping("/username/{username}")
    public ResponseEntity<ApiResponse<Void>> deleteUserByUsername(
            @Parameter(hidden = true) @CurrentUser Long currentUserId,
            @PathVariable String username,
            @Valid @RequestBody(required = false) DeleteUserRequest request) {
        log.info("회원 탈퇴 요청: username={}, currentUserId={}", username, currentUserId);

        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        // 삭제하려는 사용자가 본인인지 확인
        UserResponse user = userService.getUserByUsername(username);
        if (!currentUserId.equals(user.getUserId())) {
            log.warn("사용자 {}가 다른 사용자 {}의 탈퇴를 시도", currentUserId, username);
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        String password = request != null ? request.getPassword() : null;
        userService.deleteUserByUsername(username, password);
        return ResponseEntity.ok(ApiResponse.success(null, "회원 탈퇴가 완료되었습니다"));
    }
}
