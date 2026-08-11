package com.sparta.logistics.application.common;

import com.sparta.logistics.common.code.ErrorResponseCode;
import com.sparta.logistics.common.exception.ApiException;
import com.sparta.logistics.infrastructure.feign.client.UserClient;
import com.sparta.logistics.infrastructure.feign.dto.user.UserInfoResponse;
import com.sparta.logistics.infrastructure.feign.dto.user.UserSearchRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationChecker {

    private final UserClient userClient; // User 서비스 호출용 FeignClient

    // 이 userId가 이 companyId 담당자가 맞는지 확인
    public void checkCompanyOwnership(UUID userId, UUID targetCompanyId) {
        UserInfoResponse userInfo = getUserInfo(userId); // User 서비스에 물어봄
        if (!targetCompanyId.equals(userInfo.companyId())) {
            throw new ApiException(ErrorResponseCode.COMPANY_ACCESS_DENIED); // 아니면 403
        }
    }

    // 이 userId가 이 hubId 담당자가 맞는지 확인
    public void checkHubOwnership(UUID userId, UUID targetHubId) {
        UserInfoResponse userInfo = getUserInfo(userId);
        if (!targetHubId.equals(userInfo.hubId())) {
            throw new ApiException(ErrorResponseCode.COMPANY_ACCESS_DENIED);
        }
    }

    /**
     * UserClient로 User 서비스를 호출해 그 유저의 소속 정보(companyId, hubId)를 받아온다.
     * CircuitBreaker가 바깥쪽, Retry가 안쪽에서 동작하도록 fallback은 Retry에만 지정한다.
     * (두 어노테이션에 fallback을 동시에 지정하면 안쪽 aspect가 예외 타입을 변환시켜
     * 바깥쪽의 retry-exceptions 매칭이 깨지는 문제가 있었음 - HubValidator에서 겪었던 이슈)
     */
    @CircuitBreaker(name = "userClient")
    @Retry(name = "userClient", fallbackMethod = "getUserInfoFallback")
    public UserInfoResponse getUserInfo(UUID userId) {
        List<UserInfoResponse> result = userClient.searchUsers(new UserSearchRequest(List.of(userId)));
        return result.stream().findFirst()
                .orElseThrow(() -> new ApiException(ErrorResponseCode.COMPANY_ACCESS_DENIED));
    }

    private UserInfoResponse getUserInfoFallback(UUID userId, Throwable t) {
        log.warn("User 서비스 호출 최종 실패: userId={}, 원인={}", userId, t.getClass().getSimpleName());
        throw new ApiException(ErrorResponseCode.COMPANY_ACCESS_DENIED);
    }
}
