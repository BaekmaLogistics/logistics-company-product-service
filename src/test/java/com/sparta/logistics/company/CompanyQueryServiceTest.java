package com.sparta.logistics.company;

import com.sparta.logistics.application.query.dto.company.CompanyDetailResponseDto;
import com.sparta.logistics.application.query.dto.company.CompanyListResponseDto;
import com.sparta.logistics.application.query.dto.company.CompanySearchRequestDto;
import com.sparta.logistics.application.query.service.CompanyCacheService;
import com.sparta.logistics.application.query.service.CompanyQueryService;
import com.sparta.logistics.common.exception.ApiException;
import com.sparta.logistics.domain.entity.Company;
import com.sparta.logistics.domain.model.CompanyType;
import com.sparta.logistics.domain.repository.company.CompanyQueryRepository;
import com.sparta.logistics.domain.repository.company.CompanyRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyQueryServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyQueryRepository companyQueryRepository;

    @Mock
    private CompanyCacheService companyCacheService;

    @InjectMocks
    private CompanyQueryService companyQueryService;

    @Test
    @DisplayName("업체 상세 조회 성공 - 존재하는 업체면 DTO로 변환하여 반환")
    void get_success() {
        UUID companyId = UUID.randomUUID();
        Company company = Company.create("업체", CompanyType.SUPPLIER, UUID.randomUUID(), "주소");

        when(companyRepository.findByIdAndDeletedAtIsNull(companyId)).thenReturn(Optional.of(company));

        CompanyDetailResponseDto response = companyQueryService.get(companyId);

        assertThat(response.name()).isEqualTo("업체");
        assertThat(response.type()).isEqualTo(CompanyType.SUPPLIER);
    }

    @Test
    @DisplayName("업체 상세 조회 실패 - 존재하지 않거나 삭제된 업체면 예외 발생")
    void get_notFound_throwsException() {
        UUID companyId = UUID.randomUUID();

        when(companyRepository.findByIdAndDeletedAtIsNull(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyQueryService.get(companyId))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("업체 목록 조회 성공 - 검색조건 없이 기본 페이징 결과 반환")
    void getList_success() {
        Pageable pageable = PageRequest.of(0, 10);
        CompanySearchRequestDto condition = new CompanySearchRequestDto(null, null, null);

        Company company = Company.create("업체", CompanyType.SUPPLIER, UUID.randomUUID(), "주소");
        Page<Company> page = new PageImpl<>(List.of(company), pageable, 1);
        CompanyListResponseDto expectedResponse = CompanyListResponseDto.from(page);

        //when(companyQueryRepository.search(condition, pageable)).thenReturn(page);
        // 조건 없는 기본 조회는 캐시 서비스 경로를 타므로 companyCacheService를 stub
        when(companyCacheService.getDefaultList()).thenReturn(expectedResponse);

        CompanyListResponseDto response = companyQueryService.getList(condition, pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).name()).isEqualTo("업체");
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        verify(companyCacheService, times(1)).getDefaultList();

    }

    @Test
    @DisplayName("업체 목록 조회 - 검색조건이 Repository에 그대로 전달되는지 확인")
    void getList_passesConditionToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        UUID hubId = UUID.randomUUID();
        CompanySearchRequestDto condition = new CompanySearchRequestDto("검색어", CompanyType.SUPPLIER, hubId);

        Page<Company> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(companyQueryRepository.search(condition, pageable)).thenReturn(emptyPage);

        companyQueryService.getList(condition, pageable);

        verify(companyQueryRepository, times(1)).search(condition, pageable);
        verify(companyCacheService, never()).getDefaultList();
    }

    @Test
    @DisplayName("업체 목록 조회 - 결과가 없으면 빈 목록을 반환한다")
    void getList_empty() {
        Pageable pageable = PageRequest.of(0, 10);
        CompanySearchRequestDto condition = new CompanySearchRequestDto(null, null, null);

        Page<Company> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        CompanyListResponseDto emptyResponse = CompanyListResponseDto.from(emptyPage);
        when(companyCacheService.getDefaultList()).thenReturn(emptyResponse);

        //when(companyQueryRepository.search(condition, pageable)).thenReturn(emptyPage);

        CompanyListResponseDto response = companyQueryService.getList(condition, pageable);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
    }
}