package com.sparta.logistics.infrastructure.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record HubDeletedPayload(
        UUID hubId,
        Instant deletedAt
) {
}
