package com.sparta.logistics.presentation.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public class PageableUtils {
    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);

    public static Pageable normalize(Pageable pageable) {
        if (!ALLOWED_SIZES.contains(pageable.getPageSize())) {
            return PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
        }
        return pageable;
    }
}
