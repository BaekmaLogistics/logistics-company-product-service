package com.sparta.logistics.application.command.dto.product;

import jakarta.validation.constraints.Size;

public record ProductUpdateRequestDto(
        @Size(min = 1, message = "상품명은 빈 값일 수 없습니다") String name
) {
}
