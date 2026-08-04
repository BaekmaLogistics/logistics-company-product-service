package com.sparta.logistics.application.query.dto.company;

import com.sparta.logistics.domain.model.CompanyType;

import java.util.UUID;

public record CompanySearchRequestDto(
        String name,
        CompanyType type,
        UUID hubId
        ) {
}
