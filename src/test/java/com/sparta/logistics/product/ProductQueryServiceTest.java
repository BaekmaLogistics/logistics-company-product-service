package com.sparta.logistics.product;
import com.sparta.logistics.application.common.AuthorizationChecker;
import com.sparta.logistics.application.query.dto.product.ProductDetailResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductListResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductSearchRequestDto;
import com.sparta.logistics.application.query.dto.product.ProductSearchResult;
import com.sparta.logistics.application.query.service.ProductCacheService;
import com.sparta.logistics.application.query.service.ProductQueryService;
import com.sparta.logistics.common.exception.ApiException;
import com.sparta.logistics.domain.entity.Company;
import com.sparta.logistics.domain.entity.Product;
import com.sparta.logistics.domain.model.CompanyType;
import com.sparta.logistics.domain.model.UserRole;
import com.sparta.logistics.domain.repository.company.CompanyRepository;
import com.sparta.logistics.domain.repository.product.ProductQueryRepository;
import com.sparta.logistics.domain.repository.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductQueryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ProductQueryRepository productQueryRepository;

    @Mock
    private ProductCacheService productCacheService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private AuthorizationChecker authorizationChecker;

    @InjectMocks
    private ProductQueryService productQueryService;


    @Test
    @DisplayName("상품 상세 조회 성공 - 상품과 연결된 업체가 모두 존재하면 DTO로 변환하여 반환")
    void get_success() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Product product = Product.create("상품", companyId);
        Company company = Company.create("업체", CompanyType.SUPPLIER, UUID.randomUUID(), "주소");

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));
        when(companyRepository.findByIdAndDeletedAtIsNull(product.getCompanyId())).thenReturn(Optional.of(company));

        ProductDetailResponseDto response = productQueryService.get(productId, userId, UserRole.MASTER);

        assertThat(response.name()).isEqualTo("상품");
        assertThat(response.companyName()).isEqualTo("업체");
        verify(authorizationChecker, never()).getUserInfo(any());
    }

    @Test
    @DisplayName("상품 상세 조회 실패 - 존재하지 않거나 삭제된 상품이면 예외 발생")
    void get_productNotFound_throwsException() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productQueryService.get(productId, userId, UserRole.MASTER))
                .isInstanceOf(ApiException.class);

        verify(companyRepository, never()).findByIdAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("상품 상세 조회 실패 - 상품은 있으나 연결된 업체가 없으면 예외 발생")
    void get_companyNotFound_throwsException() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Product product = Product.create("상품", companyId);

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));
        when(companyRepository.findByIdAndDeletedAtIsNull(product.getCompanyId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productQueryService.get(productId, userId, UserRole.MASTER))
                .isInstanceOf(ApiException.class);
    }


    @Test
    @DisplayName("상품 목록 조회 성공 - 검색조건 없이 기본 페이징 결과 반환 (캐시 경로)")
    void getList_success() {
        UUID userId = UUID.randomUUID();

        Pageable pageable = PageRequest.of(0, 10);
        ProductSearchRequestDto condition = new ProductSearchRequestDto(null, null,  null);

        ProductSearchResult result = new ProductSearchResult(
                UUID.randomUUID(), "상품", UUID.randomUUID(), "업체",
                Instant.now(), Instant.now()
        );
        Page<ProductSearchResult> page = new PageImpl<>(List.of(result), pageable, 1);
        ProductListResponseDto expectedResponse = ProductListResponseDto.from(page);

        // 조건 없는 기본 조회는 캐시 서비스 경로를 타므로 productCacheService를 stub
        when(productCacheService.getDefaultList()).thenReturn(expectedResponse);

        //when(productQueryRepository.search(condition, pageable)).thenReturn(page);

        ProductListResponseDto response = productQueryService.getList(condition, pageable, userId, UserRole.MASTER);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).name()).isEqualTo("상품");
        assertThat(response.content().get(0).companyName()).isEqualTo("업체");
        assertThat(response.totalElements()).isEqualTo(1);
        verify(productCacheService, times(1)).getDefaultList();
    }

    @Test
    @DisplayName("상품 목록 조회 - companyName 조건이 Repository에 그대로 전달되는지 확인")
    void getList_passesConditionToRepository() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        ProductSearchRequestDto condition = new ProductSearchRequestDto("상품검색어", "업체검색어",null);

        Page<ProductSearchResult> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(productQueryRepository.search(condition, pageable)).thenReturn(emptyPage);

        productQueryService.getList(condition, pageable, userId, UserRole.MASTER);

        verify(productQueryRepository, times(1)).search(condition, pageable);
        verify(productCacheService, never()).getDefaultList();
    }

    @Test
    @DisplayName("상품 목록 조회 - 결과가 없으면 빈 목록을 반환한다")
    void getList_empty() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        ProductSearchRequestDto condition = new ProductSearchRequestDto(null, null,null);

        Page<ProductSearchResult> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        ProductListResponseDto emptyResponse = ProductListResponseDto.from(emptyPage);

        when(productCacheService.getDefaultList()).thenReturn(emptyResponse);

        //when(productQueryRepository.search(condition, pageable)).thenReturn(emptyPage);

        ProductListResponseDto response = productQueryService.getList(condition, pageable, userId, UserRole.MASTER);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
    }
}