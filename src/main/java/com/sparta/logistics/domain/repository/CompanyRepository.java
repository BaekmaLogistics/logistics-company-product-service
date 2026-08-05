package com.sparta.logistics.domain.repository;

import com.sparta.logistics.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findByIdAndDeletedAtIsNull(UUID id);
    boolean existsByNameAndDeletedAtIsNull(String name);
}
