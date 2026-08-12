package com.sparta.logistics.infrastructure.feign.dto.user;

import java.util.UUID;

public record UserInfoResponse (
        UUID userId,
        UUID companyId,
        UUID hubId
){
}
