package com.sparta.logistics.product;

import com.sparta.logistics.application.command.dto.product.ProductCreateRequestDto;
import com.sparta.logistics.application.command.dto.product.ProductResponseDto;
import com.sparta.logistics.application.command.dto.product.ProductUpdateRequestDto;
import com.sparta.logistics.application.command.service.ProductCommandService;
import com.sparta.logistics.common.exception.ApiException;
import com.sparta.logistics.domain.entity.Company;
import com.sparta.logistics.domain.entity.Product;
import com.sparta.logistics.domain.model.CompanyType;
import com.sparta.logistics.domain.repository.company.CompanyRepository;
import com.sparta.logistics.domain.repository.product.ProductRepository;
import com.sparta.logistics.infrastructure.feign.client.HubClient;
import com.sparta.logistics.infrastructure.feign.exception.FeignApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductCommandServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private HubClient hubClient;

    @InjectMocks
    private ProductCommandService productCommandService;

    @Test
    @DisplayName("상품 생성 성공 - 업체와 허브가 모두 유효하면 정상 생성")
    void create_success() {
        UUID companyId = UUID.randomUUID();
        UUID hubId = UUID.randomUUID();
        Company company = Company.create("업체", CompanyType.SUPPLIER, hubId, "주소");

        ProductCreateRequestDto request = new ProductCreateRequestDto("상품", companyId);

        when(companyRepository.findByIdAndDeletedAtIsNull(companyId)).thenReturn(Optional.of(company));
        when(hubClient.getHub(hubId)).thenReturn(null);

        Product savedProduct = Product.create(request.name(), request.companyId());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponseDto response = productCommandService.create(request);

        assertThat(response.name()).isEqualTo("상품");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 생성 실패 - 존재하지 않는 업체면 예외 발생")
    void create_companyNotFound_throwsException() {
        UUID companyId = UUID.randomUUID();
        ProductCreateRequestDto request = new ProductCreateRequestDto("상품", companyId);

        when(companyRepository.findByIdAndDeletedAtIsNull(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productCommandService.create(request))
                .isInstanceOf(ApiException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 생성 실패 - 업체는 있지만 소속 허브가 없으면 예외 발생")
    void create_hubNotFound_throwsException() {
        UUID companyId = UUID.randomUUID();
        UUID invalidHubId = UUID.randomUUID();
        Company company = Company.create("업체", CompanyType.SUPPLIER, invalidHubId, "주소");

        ProductCreateRequestDto request = new ProductCreateRequestDto("상품", companyId);

        when(companyRepository.findByIdAndDeletedAtIsNull(companyId)).thenReturn(Optional.of(company));
        when(hubClient.getHub(invalidHubId))
                .thenThrow(new FeignApiException("HUB_NOT_FOUND", "존재하지 않는 허브입니다.", 404));

        assertThatThrownBy(() -> productCommandService.create(request))
                .isInstanceOf(ApiException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 수정 성공 - 정상적으로 이름이 변경")
    void update_success() {
        UUID productId = UUID.randomUUID();
        Product existingProduct = Product.create("기존상품", UUID.randomUUID());

        ProductUpdateRequestDto request = new ProductUpdateRequestDto("수정된상품");

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        ProductResponseDto response = productCommandService.update(productId, request);

        assertThat(response.name()).isEqualTo("수정된상품");
    }

    @Test
    @DisplayName("상품 수정 실패 - 이미 삭제된 상품이면 예외 발생")
    void update_alreadyDeleted_throwsException() {
        UUID productId = UUID.randomUUID();
        Product deletedProduct = Product.create("상품", UUID.randomUUID());
        deletedProduct.softDelete(null);

        ProductUpdateRequestDto request = new ProductUpdateRequestDto("수정시도");

        when(productRepository.findById(productId)).thenReturn(Optional.of(deletedProduct));

        assertThatThrownBy(() -> productCommandService.update(productId, request))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("상품 수정 실패 - 존재하지 않는 상품이면 예외 발생")
    void update_productNotFound_throwsException() {
        UUID productId = UUID.randomUUID();
        ProductUpdateRequestDto request = new ProductUpdateRequestDto("수정시도");

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productCommandService.update(productId, request))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("상품 삭제 성공 - 정상적으로 소프트 삭제")
    void delete_success() {
        UUID productId = UUID.randomUUID();
        Product existingProduct = Product.create("상품", UUID.randomUUID());

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        productCommandService.delete(productId);

        assertThat(existingProduct.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("상품 삭제 - 이미 삭제된 상품 재삭제 요청은 에러 없이 종료(멱등)")
    void delete_alreadyDeleted_idempotent() {
        UUID productId = UUID.randomUUID();
        Product deletedProduct = Product.create("상품", UUID.randomUUID());
        deletedProduct.softDelete(null);
        var beforeDeletedAt = deletedProduct.getDeletedAt();

        when(productRepository.findById(productId)).thenReturn(Optional.of(deletedProduct));

        productCommandService.delete(productId);

        // 타임스탬프가 재갱신되지 않았는지(다시 삭제 처리가 안 일어났는지) 확인
        assertThat(deletedProduct.getDeletedAt()).isEqualTo(beforeDeletedAt);
    }

    @Test
    @DisplayName("상품 삭제 실패 - 존재하지 않는 상품이면 PRODUCT_NOT_FOUND 예외가 발생한다")
    void delete_productNotFound_throwsException() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productCommandService.delete(productId))
                .isInstanceOf(ApiException.class);
    }

}
