package com.sparta.logistics.application.command.dto.company;


import com.sparta.logistics.domain.entity.Company;
import com.sparta.logistics.domain.model.CompanyType;

import java.time.Instant;
import java.util.UUID;

public record CompanyResponseDto(
    UUID id,
    String name,
    CompanyType type,
    UUID hubId,
    String address,
    Instant createdAt,
    Instant updatedAt
    ) {

        public static CompanyResponseDto from(Company company) {
            return new CompanyResponseDto(
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
