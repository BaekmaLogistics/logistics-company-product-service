package com.sparta.logistics.application.command.dto.company;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CompanyUpdateRequestDto(
        @Size(min = 1, message = "업체명은 빈 값일 수 없습니다") String name,
        String address,
        UUID hubId
) {

}
