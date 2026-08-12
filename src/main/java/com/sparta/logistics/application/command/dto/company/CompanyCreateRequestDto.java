package com.sparta.logistics.application.command.dto.company;

import com.sparta.logistics.domain.model.CompanyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CompanyCreateRequestDto (
        @NotBlank(message = "업체명은 필수 입력값입니다.")
        String name,

        @NotNull(message = "업체타입은 필수 입력값입니다.")
        CompanyType type,

        @NotNull(message = "허브ID는 필수 입력값입니다.")
        UUID hubId,

        @NotBlank(message = "업체 주소는 필수 입력값입니다.")
        String address
        ){


}
