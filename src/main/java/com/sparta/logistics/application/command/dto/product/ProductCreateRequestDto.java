package com.sparta.logistics.application.command.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProductCreateRequestDto (

        @NotBlank(message = "상품명은 필수 입력값입니다.")
        String name,

        @NotNull(message = "업체를 선택해주세요.")
        UUID companyId

) {
}
