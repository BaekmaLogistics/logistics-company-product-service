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

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductQueryController {

    private final GetProductUseCase getProductUseCase;
    private final GetProductListUseCase getProductListUseCase;
    private final GetPopularProductsUseCase getPopularProductsUseCase;

    @GetMapping("/{productId}")
    public ResponseEntity<GeneralResponse<ProductDetailResponseDto>> getProduct(
            @PathVariable UUID productId,
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(HeaderConstants.USER_ROLE) String rawRole){

        UserRole role = RoleHeaderParser.parse(rawRole);
        ProductDetailResponseDto response = getProductUseCase.get(productId, userId, role);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.PRODUCT_FOUND, response);
    }

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
