package com.sparta.logistics.presentation.query.controller;

import com.sparta.logistics.application.query.dto.product.PopularProductResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductDetailResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductListResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductSearchRequestDto;
import com.sparta.logistics.application.query.usecase.product.GetPopularProductsUseCase;
import com.sparta.logistics.application.query.usecase.product.GetProductListUseCase;
import com.sparta.logistics.application.query.usecase.product.GetProductUseCase;
import com.sparta.logistics.common.code.GeneralResponseCode;
import com.sparta.logistics.domain.model.UserRole;
import com.sparta.logistics.presentation.common.constant.HeaderConstants;
import com.sparta.logistics.presentation.common.constant.RoleHeaderParser;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

        import java.util.List;
import java.util.UUID;

@Tag(name = "Product - 조회", description = "상품 단건/목록/인기상품 조회 API (허브 관리자는 담당 허브 소속으로 스코프 제한)")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductQueryController {

    private final GetProductUseCase getProductUseCase;
    private final GetProductListUseCase getProductListUseCase;
    private final GetPopularProductsUseCase getPopularProductsUseCase;

    @Operation(summary = "상품 단건 조회", description = "상품 ID로 상세 정보를 조회합니다. 허브 관리자는 담당 허브 소속 상품만 조회 가능합니다.")
    @GetMapping("/{productId}")
    public ResponseEntity<GeneralResponse<ProductDetailResponseDto>> getProduct(
            @PathVariable UUID productId,
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(HeaderConstants.USER_ROLE) String rawRole){

        UserRole role = RoleHeaderParser.parse(rawRole);
        ProductDetailResponseDto response = getProductUseCase.get(productId, userId, role);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.PRODUCT_FOUND, response);
    }

    @Operation(summary = "상품 목록 조회", description = "상품명/업체명/허브 조건으로 검색 가능하며, 허브 관리자는 담당 허브로 검색 범위가 강제됩니다.")
    @GetMapping
    public ResponseEntity<GeneralResponse<ProductListResponseDto>> getProductList(
            ProductSearchRequestDto condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(HeaderConstants.USER_ROLE) String rawRole) {

        UserRole role = RoleHeaderParser.parse(rawRole);
        ProductListResponseDto response = getProductListUseCase.getList(condition, pageable, userId, role);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.PRODUCT_LIST_FOUND, response);
    }

    @Operation(summary = "인기 상품 조회", description = "누적 주문 수량 기준 상위 N개 상품을 조회합니다. (limit: 1~100, 기본 10)")
    @GetMapping("/popular")
    public ResponseEntity<GeneralResponse<List<PopularProductResponseDto>>> getPopularProducts(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(HeaderConstants.USER_ROLE) String rawRole) {

        UserRole role = RoleHeaderParser.parse(rawRole);
        List<PopularProductResponseDto> response = getPopularProductsUseCase.getPopularProducts(limit, userId, role);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.PRODUCT_LIST_FOUND, response);
    }
}
