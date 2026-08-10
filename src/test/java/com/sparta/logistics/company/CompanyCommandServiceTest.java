package com.sparta.logistics.company;

import com.sparta.logistics.application.command.dto.company.CompanyCreateRequestDto;
import com.sparta.logistics.application.command.dto.company.CompanyResponseDto;
import com.sparta.logistics.application.command.dto.company.CompanyUpdateRequestDto;
import com.sparta.logistics.application.command.service.CompanyCommandService;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyCommandServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private HubClient hubClient;

    @InjectMocks
    private CompanyCommandService companyCommandService;

    @Test
    @DisplayName("업체 생성 성공 - hubId가 유효하고 이름 중복이 없으면 정상 생성")
    void company_create_success() {
        // given : 유효한 요청, hubId 검증 통과, 이름 중복 없음을 가정
        UUID hubId = UUID.randomUUID();
        CompanyCreateRequestDto request = new CompanyCreateRequestDto(
                "테스트업체", CompanyType.SUPPLIER, hubId, "서울시 어딘가"
        );

        // hubClient.getHub()가 예외없이 통과한다고 가정(정상 허브)
        when(hubClient.getHub(hubId)).thenReturn(null);

        // 이름 중복 없음
        when(companyRepository.existsByNameAndDeletedAtIsNull(request.name())).thenReturn(false);

        //save 호출 시 실제 저장된 것처럼 id가 채워진 Company를 반환하도록 목킹
        Company savedCompany = Company.create(request.name(), request.type(), request.hubId(), request.address());
        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        //when
        CompanyResponseDto response = companyCommandService.create(request);

        // then : 응답이 요청한 값과 일치하는지 확인
        assertThat(response.name()).isEqualTo("테스트업체");
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("업체 생성 실패 - 존재하지 않는 hubId면 예외 발생")
    void create_hubNotFound_throwsException() {
        // given : hubClient가 존재하지 않는 허브에 대해 예외를 던지는 상황을 가정
        UUID invalidHubId = UUID.randomUUID();
        CompanyCreateRequestDto request = new CompanyCreateRequestDto(
                "테스트업체", CompanyType.SUPPLIER, invalidHubId, "서울시 어딘가"
        );

        when(hubClient.getHub(invalidHubId)).thenThrow(
                new FeignApiException("HUB_NOT_FOUND", "존재하지 않는 허브입니다.", 404));

        //when & then  : ApiException이 발생하고, 저장 로직까지 도달하지 않아야 함
        assertThatThrownBy(()-> companyCommandService.create(request))
                .isInstanceOf(ApiException.class);

        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("업체 생성 실패 - 이름이 중복되면 예외 발생")
    void create_nameDuplicated_throwsException() {
        // given : hubId 검증은 통과, 이름이 이미 존재하는 경우
        UUID hubId = UUID.randomUUID();
        CompanyCreateRequestDto request = new CompanyCreateRequestDto(
                "중복업체", CompanyType.SUPPLIER, hubId, "서울시 어딘가"
        );
        when(hubClient.getHub(hubId)).thenReturn(null);
        when(companyRepository.existsByNameAndDeletedAtIsNull(request.name())).thenReturn(true);

        // when & then
        assertThatThrownBy(()->companyCommandService.create(request))
                .isInstanceOf(ApiException.class);

        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("업체 수정 성공 - 유효한 hubId로 수정하면 정상 반영")
    void update_success() {
        // given : 기존에 저장된 업체가 있고, 새로운 hubId로 수정을 요청하는 상황
        UUID companyId = UUID.randomUUID();
        UUID newHubId = UUID.randomUUID();
        Company existingCompany = Company.create("기존업체", CompanyType.SUPPLIER, UUID.randomUUID(), "기존주소");

        CompanyUpdateRequestDto request = new CompanyUpdateRequestDto("수정된업체명", "수정된주소", newHubId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany));
        when(hubClient.getHub(newHubId)).thenReturn(null);

        // when
        CompanyResponseDto response = companyCommandService.update(companyId, request);

        // then : 실제로 name, address, hubId가 바뀌었는지 확인
        assertThat(response.name()).isEqualTo("수정된업체명");
    }

    @Test
    @DisplayName("업체 수정 실패 - 존재하지 않는 hubId로 수정하면 예외가 발생")
    void update_hubNotFound_throwsException() {
        // given
        UUID companyId = UUID.randomUUID();
        UUID invalidHubId = UUID.randomUUID();
        Company existingCompany = Company.create("기존업체", CompanyType.SUPPLIER, UUID.randomUUID(), "기존주소");

        CompanyUpdateRequestDto request = new CompanyUpdateRequestDto("수정된업체명", "수정된주소", invalidHubId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany));
        when(hubClient.getHub(invalidHubId))
                        .thenThrow(new FeignApiException("HUB_NOT_FOUND", "존재하지 않는 허브입니다.", 404));

        // when & then
        assertThatThrownBy(() -> companyCommandService.update(companyId, request))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("업체 수정 성공 - hubId 없이 요청하면 검증 없이 name/address만 수정")
    void update_withoutHubId_skipsValidation() {
        // given : hubId를 안 보내는(null) 부분수정 요청
        UUID companyId = UUID.randomUUID();
        Company existingCompany = Company.create("기존업체", CompanyType.SUPPLIER, UUID.randomUUID(), "기존주소");

        CompanyUpdateRequestDto request = new CompanyUpdateRequestDto("수정된업체명", null, null);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany));

        // when
        CompanyResponseDto response = companyCommandService.update(companyId, request);

        // then : hubClient가 아예 호출되지 않아야 함 (검증 스킵 확인)
        assertThat(response.name()).isEqualTo("수정된업체명");
        verify(hubClient, never()).getHub(any());
    }

    @Test
    @DisplayName("업체 수정 실패 - 존재하지 않는 업체를 수정하려 하면 예외 발생")
    void update_companyNotFound_throwsException() {
        // given
        UUID companyId = UUID.randomUUID();
        CompanyUpdateRequestDto request = new CompanyUpdateRequestDto("이름", "주소", null);

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> companyCommandService.update(companyId, request))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("업체 삭제 성공 - 정상 삭제되며 소속 상품도 함께 비활성화")
    void delete_success_cascadesToProducts() {
        // given : 삭제되지 않은 업체와, 그 업체에 소속된 상품 2개가 있는 상황
        UUID companyId = UUID.randomUUID();
        Company existingCompany = Company.create("업체", CompanyType.SUPPLIER, UUID.randomUUID(), "주소");

        Product product1 = Product.create("상품1", companyId);
        Product product2 = Product.create("상품2", companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany));
        when(productRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId))
                .thenReturn(List.of(product1, product2));

        // when
        companyCommandService.delete(companyId);

        // then : 상품 조회가 실제로 호출됐는지 확인 (연쇄 삭제 로직이 실행됐다는 증거)
        verify(productRepository, times(1)).findAllByCompanyIdAndDeletedAtIsNull(companyId);
        assertThat(product1.getDeletedAt()).isNotNull();
        assertThat(product2.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("업체 삭제 - 이미 삭제된 업체를 재삭제 요청하면 에러 없이 종료(멱등)")
    void delete_alreadyDeleted_idempotent() {
        // given : 이미 삭제 처리된 업체
        UUID companyId = UUID.randomUUID();
        Company alreadyDeletedCompany = Company.create("업체", CompanyType.SUPPLIER, UUID.randomUUID(), "주소");
        alreadyDeletedCompany.softDelete(null); // 이미 삭제된 상태로 만듦

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(alreadyDeletedCompany));

        // when
        companyCommandService.delete(companyId);

        // then : 상품 조회 로직 자체가 호출되지 않아야 함 (멱등 처리로 조기 종료)
        verify(productRepository, never()).findAllByCompanyIdAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("업체 삭제 실패 - 존재한 적 없는 업체를 삭제하려 하면 예외 발생")
    void delete_companyNotFound_throwsException() {
        // given
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> companyCommandService.delete(companyId))
                .isInstanceOf(ApiException.class);
    }
}
