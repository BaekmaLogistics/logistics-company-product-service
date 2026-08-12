package com.sparta.logistics.infrastructure.messaging.listener;

import com.sparta.logistics.application.command.service.CompanyCommandService;
import com.sparta.logistics.infrastructure.messaging.envelope.EventEnvelope;
import com.sparta.logistics.infrastructure.messaging.event.HubDeletedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubDeletedEventListener {
    private final CompanyCommandService companyCommandService;

    @RabbitListener(queues = "${message.queue.company-hub-deleted}")
    public void handleHubDeleted(EventEnvelope<HubDeletedPayload> envelope) {
        if(!"HubDeleted".equals(envelope.header().eventType())) {
            return;
        }

        HubDeletedPayload payload = envelope.payload();
        log.info("Hub 삭제 이벤트 수신: hubId={}", payload.hubId());
        companyCommandService.deactivateByHubId(payload.hubId());
    }
}
