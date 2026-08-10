package com.sparta.logistics.application.common;

import com.sparta.logistics.common.code.ErrorResponseCode;
import com.sparta.logistics.common.exception.ApiException;
import com.sparta.logistics.infrastructure.feign.client.UserClient;
import com.sparta.logistics.infrastructure.feign.dto.user.UserInfoResponse;
import com.sparta.logistics.infrastructure.feign.dto.user.UserSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

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

    private UserInfoResponse getUserInfo(UUID userId) {
        // UserClient로 User 서비스 호출해서 그 유저의 소속 정보(companyId, hubId) 받아옴
        List<UserInfoResponse> result = userClient.searchUsers(new UserSearchRequest(List.of(userId)));
        return result.stream().findFirst()
                .orElseThrow(() -> new ApiException(ErrorResponseCode.COMPANY_ACCESS_DENIED));
    }
}
