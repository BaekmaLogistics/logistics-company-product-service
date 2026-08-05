package com.sparta.logistics.application.query.dto.product;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductSearchRequestDto(
        String name,
        String companyName
) {
}
