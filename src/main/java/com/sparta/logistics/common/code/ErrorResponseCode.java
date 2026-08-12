package com.sparta.logistics.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorResponseCode implements ApiResponseCode {
    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"COMMON_0001", "알 수 없는 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_0002","유효하지 않은 요청입니다."),
    FEIGN_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, "COMMON_0003", "Feign 통신 중 오류가 발생했습니다."),

    // Company
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY_0001", "존재하지 않는 업체입니다."),
    COMPANY_NAME_DUPLICATED(HttpStatus.CONFLICT, "COMPANY_0002", "이미 존재하는 업체명입니다."),
    COMPANY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMPANY_0003", "해당 업체에 대한 권한이 없습니다."),
    COMPANY_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "COMPANY_0004", "이미 삭제된 업체입니다."),
    HUB_NOT_FOUND(HttpStatus.BAD_REQUEST, "COMPANY_0005", "존재하지 않는 허브입니다."),
    HUB_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "COMPANY_0006", "허브 서비스와 통신할 수 없습니다."),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_0001", "존재하지 않는 상품입니다."),
    PRODUCT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PRODUCT_0002", "해당 상품에 대한 권한이 없습니다."),
    PRODUCT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "PRODUCT_0003", "이미 삭제된 상품입니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
