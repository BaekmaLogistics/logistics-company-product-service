package com.sparta.logistics.application.common;

import com.sparta.logistics.common.code.ErrorResponseCode;
import com.sparta.logistics.common.exception.ApiException;
import com.sparta.logistics.infrastructure.feign.client.HubClient;
import com.sparta.logistics.infrastructure.feign.dto.hub.HubApiResponse;
import com.sparta.logistics.infrastructure.feign.dto.hub.HubResponseDto;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * hubId로 Hub 서비스를 조회한다.
     * CircuitBreaker가 바깥쪽, Retry가 안쪽에서 동작하도록 어노테이션 순서를 유지해야
     * 재시도 중 발생한 실패가 CircuitBreaker의 실패율 집계에 정상 반영된다.
     * (Resilience4j 기본 aspect order: CircuitBreaker > RateLimiter > Retry)
     */
    @CircuitBreaker(name = "hubClient")
    @Retry(name = "hubClient", fallbackMethod = "hubValidationFallback")
    public HubApiResponse<HubResponseDto> fetchHub(UUID hubId) {
        return hubClient.getHub(hubId);
    }


    /**
     * 재시도 소진 또는 CircuitBreaker Open 상태에서 호출되어,
     * 존재하지 않거나 접근 불가능한 허브로 간주하고 ApiException을 던진다.
     * 두 어노테이션의 fallback 시그니처가 동일해야 하며, 이 메서드가 공용으로 사용된다.
     */
    private HubApiResponse<HubResponseDto> hubValidationFallback(UUID hubId, Throwable t) {
        log.warn("허브 검증 최종 실패: hubId={}, 원인={}", hubId, t.getClass().getSimpleName());
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
        circuitBreakerRegistry.circuitBreaker("hubClient").getEventPublisher()
                .onStateTransition(event -> log.warn("HubClient CircuitBreaker 상태 전이: {} -> {}",
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()));
    }

}
