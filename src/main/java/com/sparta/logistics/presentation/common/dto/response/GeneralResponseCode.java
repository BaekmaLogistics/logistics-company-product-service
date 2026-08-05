package com.sparta.logistics.presentation.common.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum GeneralResponseCode implements ApiResponseCode {
    // Common
    OK(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "성공적으로 생성되었습니다."),

    // Company
    COMPANY_CREATED(HttpStatus.CREATED, "업체 등록 성공"),
    COMPANY_UPDATED(HttpStatus.OK, "업체 정보 수정 성공"),
    COMPANY_DELETED(HttpStatus.OK, "업체 삭제 성공"),
    COMPANY_FOUND(HttpStatus.OK, "업체 상세조회 성공"),
    COMPANY_LIST_FOUND(HttpStatus.OK, "업체 목록 조회 성공"),

    // Product
    PRODUCT_CREATED(HttpStatus.CREATED, "상품 등록 성공"),
    PRODUCT_UPDATED(HttpStatus.OK, "상품 정보 수정 성공"),
    PRODUCT_DELETED(HttpStatus.OK, "상품 삭제 성공"),
    PRODUCT_FOUND(HttpStatus.OK, "상품 상세조회 성공"),
    PRODUCT_LIST_FOUND(HttpStatus.OK, "상품 목록 조회 성공"),
    PRODUCT_BULK_CREATED(HttpStatus.OK, "상품 대량등록 완료"),
    PRODUCT_FREQUENCY_FOUND(HttpStatus.OK, "허브별 출고 빈도 TOP N 조회 성공");


    public int getCode() {
        return this.status.value();
    }

    private final HttpStatus status;
    private final String message;
}