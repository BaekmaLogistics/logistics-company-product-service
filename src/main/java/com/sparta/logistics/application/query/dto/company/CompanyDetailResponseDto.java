package com.sparta.logistics.application.query.dto.company;

import com.sparta.logistics.domain.entity.Company;
import com.sparta.logistics.domain.model.CompanyType;

import java.time.Instant;
import java.util.UUID;

public record CompanyDetailResponseDto (
        UUID id,
        String name,
        CompanyType type,
        UUID hubId,
        String address,
        Instant createdAt,
        Instant updatedAt
) {

    public static CompanyDetailResponseDto from(Company company) {
        return new CompanyDetailResponseDto(
                company.getId(),
                company.getName(),
                company.getType(),
                company.getHubId(),
                company.getAddress(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}

