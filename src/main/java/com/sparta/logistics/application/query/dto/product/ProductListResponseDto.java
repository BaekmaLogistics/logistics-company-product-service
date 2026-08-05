package com.sparta.logistics.application.query.dto.product;

import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductListResponseDto(
        List<ProductDetailResponseDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static ProductListResponseDto from(Page<ProductSearchResult> page) {
        List<ProductDetailResponseDto> content = page.getContent().stream()
                .map(req -> new ProductDetailResponseDto(
                        req.id(), req.name(), req.companyId(), req.companyName(), req.createdAt(), req.updatedAt()))
                .toList();

        return new ProductListResponseDto(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

}
