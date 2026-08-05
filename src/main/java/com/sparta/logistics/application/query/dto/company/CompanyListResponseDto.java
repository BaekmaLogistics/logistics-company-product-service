package com.sparta.logistics.application.query.dto.company;

import com.sparta.logistics.domain.entity.Company;
import org.springframework.data.domain.Page;

import java.util.List;

public record CompanyListResponseDto (

        List<CompanyDetailResponseDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
){
    public static CompanyListResponseDto from(Page<Company> page) {
        List<CompanyDetailResponseDto> content = page.getContent().stream()
                .map(CompanyDetailResponseDto::from)
                .toList();

        return new CompanyListResponseDto(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
