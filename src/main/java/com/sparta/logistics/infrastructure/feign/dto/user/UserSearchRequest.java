package com.sparta.logistics.infrastructure.feign.dto.user;

import java.util.List;
import java.util.UUID;

public record UserSearchRequest (
        List<UUID> userIds
) {
}
