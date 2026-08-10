package com.sparta.logistics.presentation.common.constant;

import com.sparta.logistics.common.code.ErrorResponseCode;
import com.sparta.logistics.common.exception.ApiException;
import com.sparta.logistics.domain.model.UserRole;

public final class RoleHeaderParser {

    private static final String ROLE_PREFIX = "ROLE_";

    private RoleHeaderParser() {
    }

    public static UserRole parse(String role) {
        if (role == null || role.isBlank()) {
            throw new ApiException(ErrorResponseCode.INVALID_REQUEST);
        }

        String normalized = role.startsWith(ROLE_PREFIX)
                ? role.substring(ROLE_PREFIX.length())
                : role;

        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorResponseCode.INVALID_REQUEST);
        }
    }
}