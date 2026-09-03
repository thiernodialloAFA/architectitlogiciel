package com.pvg.governance.web;

import com.pvg.governance.domain.AuditEntityType;
import com.pvg.governance.domain.AuditEvent;
import com.pvg.governance.service.AuditService;
import com.pvg.governance.api.AuditDtos.AuditEventResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Read-only: the audit trail is append-only and exposes no mutation endpoints. */
@RestController
@RequestMapping("/api/audit-events")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public List<AuditEventResponse> list(@RequestParam(required = false) AuditEntityType entityType,
                                         @RequestParam(required = false) UUID entityId) {
        List<AuditEvent> events = (entityType != null && entityId != null)
                ? auditService.eventsFor(entityType, entityId)
                : auditService.recentEvents();
        return events.stream()
                .map(event -> new AuditEventResponse(event.getId(), event.getEntityType(), event.getEntityId(),
                        event.getAction(), event.getFieldName(), event.getOldValue(), event.getNewValue(),
                        event.getActor(), event.getOccurredAt()))
                .toList();
    }
}
