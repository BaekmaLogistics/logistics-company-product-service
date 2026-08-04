package com.sparta.logistics.domain.repository;

import com.sparta.logistics.application.query.dto.company.CompanySearchRequestDto;
import com.sparta.logistics.domain.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyQueryRepository {
    Page<Company> search(CompanySearchRequestDto condition, Pageable pageable);
}
