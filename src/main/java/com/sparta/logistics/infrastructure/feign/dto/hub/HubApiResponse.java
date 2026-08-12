package com.sparta.logistics.infrastructure.feign.dto.hub;

public record HubApiResponse<T>(
        String message,
        T data
) {
}
