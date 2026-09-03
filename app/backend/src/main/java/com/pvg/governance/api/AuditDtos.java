package com.pvg.governance.api;

import com.pvg.governance.domain.AuditAction;
import com.pvg.governance.domain.AuditEntityType;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class AuditDtos {

    private AuditDtos() {
    }

    public record AuditEventResponse(
            Long id,
            AuditEntityType entityType,
            UUID entityId,
            AuditAction action,
            String fieldName,
            String oldValue,
            String newValue,
            String actor,
            OffsetDateTime occurredAt) {
    }
}
