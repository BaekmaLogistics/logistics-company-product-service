package com.sparta.logistics.infrastructure.feign.dto;

public record HubApiResponse<T>(
        String message,
        T data
) {
}
