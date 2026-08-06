package com.sparta.logistics.application.command.dto.company;

import java.util.UUID;

public record CompanyUpdateRequestDto(
        String name,
        String address,
        UUID hubId
) {

}
