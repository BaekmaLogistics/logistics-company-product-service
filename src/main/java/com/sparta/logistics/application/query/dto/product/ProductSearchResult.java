package com.sparta.logistics.application.query.dto.product;

import java.time.Instant;
import java.util.UUID;

public record ProductSearchResult(
        UUID id,
        String name,
        UUID companyId,
        String companyName,
        Instant createdAt,
        Instant updatedAt
) {
}
