package com.sparta.logistics.infrastructure.feign.client;


import com.sparta.logistics.infrastructure.feign.dto.hub.HubApiResponse;
import com.sparta.logistics.infrastructure.feign.dto.hub.HubResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-client", url = "${hub.service.url}")
public interface HubClient {

    @GetMapping("/internal/api/v1/hubs/{hubId}")
    HubApiResponse<HubResponseDto> getHub(@PathVariable("hubId") UUID hubId);
}
