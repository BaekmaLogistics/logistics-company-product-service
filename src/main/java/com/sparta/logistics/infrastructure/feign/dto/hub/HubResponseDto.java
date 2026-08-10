package com.sparta.logistics.infrastructure.feign.dto.hub;

import java.util.UUID;

public record HubResponseDto (
        UUID id,
        String name,
        String address
){

}
