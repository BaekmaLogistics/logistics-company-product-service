package com.sparta.logistics.presentation.query.controller;

import com.sparta.logistics.application.query.dto.product.PopularProductResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductDetailResponseDto;
import com.sparta.logistics.application.query.usecase.product.GetPopularProductsUseCase;
import com.sparta.logistics.application.query.usecase.product.GetProductUseCase;
import com.sparta.logistics.common.code.GeneralResponseCode;
import com.sparta.logistics.domain.model.UserRole;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Product - 내부 API", description = "다른 서비스가 Feign으로 호출하는 상품 내부 조회 API")
@RestController
@RequestMapping("/internal/api/v1/products")
@RequiredArgsConstructor
public class ProductInternalController {

    private final GetProductUseCase getProductUseCase;

    @SecurityRequirements
    @GetMapping("/{productId}")
    @Operation(summary = "상품 단건 조회 (내부용)", description = "서비스 간 호출 전용 엔드포인트로, 인증 헤더 없이 MASTER 권한으로 조회합니다.")
    public ResponseEntity<GeneralResponse<ProductDetailResponseDto>> getProduct(
            @PathVariable UUID productId) {

        ProductDetailResponseDto response = getProductUseCase.get(productId, null, UserRole.MASTER);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.PRODUCT_FOUND, response);
    }
}
