package com.sparta.logistics.application.common;

import com.sparta.logistics.infrastructure.feign.dto.hub.HubApiResponse;
import com.sparta.logistics.infrastructure.feign.dto.hub.HubResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Hub 정보(hubId 존재 검증 결과) 캐싱 전용 컴포넌트.
 * 17개 고정 허브라 변경 빈도가 매우 낮으므로, companyList/productList보다
 * 긴 TTL을 RedisConfig에서 별도 적용한다.
 * HubDeletedEventListener에서 허브 삭제 이벤트 수신 시 evictHub 호출로 정합성 유지.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HubCacheService {

    private final HubValidator hubValidator;

    @Cacheable(value = "hubInfo", key = "#hubId")
    public HubApiResponse<HubResponseDto> getHub(UUID hubId) {
        log.info("[캐시 미스] Hub 서비스에 실제 조회 요청: hubId={}", hubId);
        return hubValidator.fetchHub(hubId);
    }

    @CacheEvict(value = "hubInfo", key = "#hubId")
    public void evictHub(UUID hubId) {
        log.info("[캐시 무효화] hubId={}", hubId);
    }
}
