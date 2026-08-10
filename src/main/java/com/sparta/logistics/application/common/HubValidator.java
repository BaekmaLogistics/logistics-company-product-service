package com.sparta.logistics.application.common;

import com.sparta.logistics.common.code.ErrorResponseCode;
import com.sparta.logistics.common.exception.ApiException;
import com.sparta.logistics.infrastructure.feign.client.HubClient;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Hub 서비스에 FeignClient로 hubId 존재 여부를 검증하고,
 * 실패 시 Resilience4j를 통해 재시도 및 예외 변환을 담당하는 컴포넌트.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HubValidator {

    private  final HubClient hubClient;
    private final RetryRegistry retryRegistry;

    /**
     * 주어진 hubId가 Hub 서비스에 실제로 존재하는지 검증하며,
     * 통신 실패 시 설정된 재시도 정책에 따라 자동으로 재시도.
     */
    @Retry(name = "hubClient", fallbackMethod = "hubValidationFallback")
    public void validateHub(UUID hubId) {
        hubClient.getHub(hubId);
    }


    /**
     * 재시도를 모두 소진한 뒤에도 Hub 조회가 실패하면 호출되어,
     * 존재하지 않는 허브로 간주하고 ApiException을 던진다.
     */
    private void hubValidationFallback(UUID hubId, Throwable t) {
        log.warn("허브 검증 최종 실패 (재시도 소진): hubId={}", hubId);
        throw new ApiException(ErrorResponseCode.HUB_NOT_FOUND);
    }

    /**
     * 빈 초기화 시점에 hubClient 재시도(Retry) 이벤트 리스너를 등록하여,
     * 재시도가 발생할 때마다 시도 횟수와 실패 원인을 로그로 남긴다.
     */
    @PostConstruct
    public void registerRetryEventListener() {
        retryRegistry.retry("hubClient").getEventPublisher()
                .onRetry(event -> {
                    Throwable cause = event.getLastThrowable();
                    log.info("재시도 발생: {}회차, 원인={}",
                            event.getNumberOfRetryAttempts(),
                            cause != null ? cause.getMessage() : "알 수 없음");
                });
    }

}
