package com.hamkkebu.authservice.grpc.client;

import com.hamkkebu.authservice.grpc.user.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 서비스 gRPC 클라이언트
 *
 * <p>Zero-Payload 패턴: Kafka 이벤트에서 userId를 받아 사용자 전체 정보를 조회</p>
 *
 * <p>Circuit Breaker 패턴 적용:</p>
 * <ul>
 *   <li>gRPC 서비스 장애 시 자동 fallback</li>
 *   <li>일정 실패율 초과 시 Circuit Open (빠른 실패)</li>
 *   <li>주기적으로 Half-Open 상태로 복구 시도</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@literal @}Autowired
 * private UserServiceClient userServiceClient;
 *
 * // Event Listener에서:
 * Optional{@literal <}User{@literal >} userOpt = userServiceClient.getUser(event.getUserId());
 * if (userOpt.isPresent()) {
 *     User user = userOpt.get();
 *     // 비즈니스 로직 수행...
 * }
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceClient {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    private static final String CIRCUIT_BREAKER_NAME = "userService";

    /**
     * 사용자 ID로 사용자 정보 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 정보 (Optional)
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getUserFallback")
    public Optional<User> getUser(String userId) {
        log.debug("[gRPC Client] GetUser request: userId={}", userId);

        try {
            GetUserRequest request = GetUserRequest.newBuilder()
                .setUserId(userId)
                .build();

            GetUserResponse response = userServiceStub.getUser(request);

            if (response.hasUser()) {
                log.info("[gRPC Client] GetUser success: userId={}", userId);
                return Optional.of(response.getUser());
            } else if (!response.getErrorMessage().isEmpty()) {
                log.warn("[gRPC Client] GetUser failed: userId={}, error={}",
                    userId, response.getErrorMessage());
                return Optional.empty();
            }

            return Optional.empty();

        } catch (StatusRuntimeException e) {
            log.error("[gRPC Client] GetUser gRPC error: userId={}, status={}, error={}",
                userId, e.getStatus(), e.getMessage());
            throw e;  // Circuit Breaker가 처리
        }
    }

    /**
     * 여러 사용자 ID로 배치 조회
     *
     * @param userIds 사용자 ID 목록
     * @return 사용자 정보 목록
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getUsersFallback")
    public List<User> getUsers(List<String> userIds) {
        log.debug("[gRPC Client] GetUsers request: count={}", userIds.size());

        try {
            GetUsersRequest request = GetUsersRequest.newBuilder()
                .addAllUserIds(userIds)
                .build();

            GetUsersResponse response = userServiceStub.getUsers(request);

            log.info("[gRPC Client] GetUsers success: found={}/{}",
                response.getUsersCount(), userIds.size());
            return response.getUsersList();

        } catch (StatusRuntimeException e) {
            log.error("[gRPC Client] GetUsers gRPC error: count={}, status={}, error={}",
                userIds.size(), e.getStatus(), e.getMessage());
            throw e;
        }
    }

    /**
     * 사용자 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "userExistsFallback")
    public boolean userExists(String userId) {
        log.debug("[gRPC Client] UserExists request: userId={}", userId);

        try {
            UserExistsRequest request = UserExistsRequest.newBuilder()
                .setUserId(userId)
                .build();

            UserExistsResponse response = userServiceStub.userExists(request);

            return response.getExists();

        } catch (StatusRuntimeException e) {
            log.error("[gRPC Client] UserExists gRPC error: userId={}, status={}, error={}",
                userId, e.getStatus(), e.getMessage());
            throw e;
        }
    }

    // ==================== Fallback Methods ====================

    /**
     * getUser 실패 시 Fallback (Circuit Breaker)
     */
    private Optional<User> getUserFallback(String userId, Exception e) {
        log.error("[gRPC Client] GetUser fallback triggered: userId={}, error={}",
            userId, e.getMessage());
        return Optional.empty();
    }

    /**
     * getUsers 실패 시 Fallback (Circuit Breaker)
     */
    private List<User> getUsersFallback(List<String> userIds, Exception e) {
        log.error("[gRPC Client] GetUsers fallback triggered: count={}, error={}",
            userIds.size(), e.getMessage());
        return List.of();
    }

    /**
     * userExists 실패 시 Fallback (Circuit Breaker)
     */
    private boolean userExistsFallback(String userId, Exception e) {
        log.error("[gRPC Client] UserExists fallback triggered: userId={}, error={}",
            userId, e.getMessage());
        return false;  // 안전한 기본값
    }
}
