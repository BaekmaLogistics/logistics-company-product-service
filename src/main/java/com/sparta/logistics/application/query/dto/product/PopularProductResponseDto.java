package com.sparta.logistics.application.query.dto.product;

import java.util.UUID;

public record PopularProductResponseDto(
        UUID productId,
        double orderCount
) {

}