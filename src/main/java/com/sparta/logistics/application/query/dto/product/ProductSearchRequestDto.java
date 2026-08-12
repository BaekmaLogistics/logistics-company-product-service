package com.sparta.logistics.application.query.dto.product;

import java.util.UUID;

public record ProductSearchRequestDto(
        String name,
        String companyName,
        UUID hubId
) {
}
