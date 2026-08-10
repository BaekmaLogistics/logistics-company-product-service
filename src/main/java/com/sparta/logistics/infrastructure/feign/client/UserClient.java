package com.sparta.logistics.infrastructure.feign.client;

import com.sparta.logistics.infrastructure.feign.dto.user.UserInfoResponse;
import com.sparta.logistics.infrastructure.feign.dto.user.UserSearchRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserClient {
    @PostMapping("/internal/api/v1/users/search")
    List<UserInfoResponse> searchUsers(@RequestBody UserSearchRequest request);
}
