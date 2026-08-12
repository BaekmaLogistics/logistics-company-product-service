package com.sparta.logistics.presentation.command.controller;


import com.sparta.logistics.application.command.dto.product.ProductCreateRequestDto;
import com.sparta.logistics.application.command.dto.product.ProductResponseDto;
import com.sparta.logistics.application.command.dto.product.ProductUpdateRequestDto;
import com.sparta.logistics.application.command.usecase.product.CreateProductUseCase;
import com.sparta.logistics.application.command.usecase.product.DeleteProductUseCase;
import com.sparta.logistics.application.command.usecase.product.UpdateProductUseCase;
import com.sparta.logistics.common.code.GeneralResponseCode;
import com.sparta.logistics.domain.model.UserRole;
import com.sparta.logistics.presentation.common.constant.HeaderConstants;
import com.sparta.logistics.presentation.common.constant.RoleHeaderParser;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Product - 등록/수정/삭제", description = "상품 생성, 수정, 삭제 API (마스터 관리자/허브 관리자/업체 담당자 권한 필요)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductCommandController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    // 상품 등록
    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다. 소속 업체가 유효해야 합니다.")
    @PostMapping
    public ResponseEntity<GeneralResponse<ProductResponseDto>> createProduct(
            @Valid @RequestBody ProductCreateRequestDto request,
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(HeaderConstants.USER_ROLE) String rawRole) {

        UserRole role = RoleHeaderParser.parse(rawRole);

        ProductResponseDto response = createProductUseCase.create(request, userId, role);

        return GeneralResponse.toResponseEntity(GeneralResponseCode.PRODUCT_CREATED, response);
    }

    // 상품 수정
    @Operation(summary = "상품 수정", description = "상품 정보를 수정합니다.")
    @PatchMapping("/{productId}")
    public ResponseEntity<GeneralResponse<ProductResponseDto>> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpdateRequestDto request,
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(HeaderConstants.USER_ROLE) String rawRole) {

        UserRole role = RoleHeaderParser.parse(rawRole);

        ProductResponseDto response = updateProductUseCase.update(productId, request, userId, role);

        return GeneralResponse.toResponseEntity(GeneralResponseCode.PRODUCT_UPDATED, response);
    }

    // 상품 삭제
    @Operation(summary = "상품 삭제", description = "상품을 논리 삭제합니다. 인기 상품 집계 캐시에서도 함께 제거됩니다.")
    @DeleteMapping("/{productId}")
    public ResponseEntity<GeneralResponse<Void>> deleteProduct(
            @PathVariable UUID productId,
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(HeaderConstants.USER_ROLE) String rawRole) {

        UserRole role = RoleHeaderParser.parse(rawRole);

        deleteProductUseCase.delete(productId, userId, role);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.PRODUCT_DELETED, null);
    }

}
