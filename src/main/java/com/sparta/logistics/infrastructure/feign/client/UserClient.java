package com.sparta.logistics.infrastructure.feign.client;

import com.sparta.logistics.infrastructure.feign.dto.user.UserInfoResponse;
import com.sparta.logistics.infrastructure.feign.dto.user.UserSearchRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserClient {
    @GetMapping("/internal/api/v1/users/{userId}")
    UserInfoResponse getUser(@PathVariable("userId") UUID userId);
}
