package com.pvg.governance.service;

import com.pvg.governance.domain.AuditAction;
import com.pvg.governance.domain.AuditEntityType;
import com.pvg.governance.domain.AuditEvent;
import com.pvg.governance.repository.AuditEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Append-only audit trail (proposal §5). Events are written in the same transaction
 * as the change they record (callers are transactional services); there is no API to
 * modify or remove events.
 */
@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void recordCreated(AuditEntityType type, UUID entityId, String summary, String actor) {
        auditEventRepository.save(new AuditEvent(type, entityId, AuditAction.CREATED, null, null, summary, actor));
    }

    public void recordFieldChange(AuditEntityType type, UUID entityId, String field,
                                  Object oldValue, Object newValue, String actor) {
        if (Objects.equals(asString(oldValue), asString(newValue))) {
            return;
        }
        auditEventRepository.save(new AuditEvent(type, entityId, AuditAction.FIELD_CHANGED,
                field, asString(oldValue), asString(newValue), actor));
    }

    public void recordStatusChange(AuditEntityType type, UUID entityId,
                                   Object oldStatus, Object newStatus, String actor) {
        auditEventRepository.save(new AuditEvent(type, entityId, AuditAction.STATUS_CHANGED,
                "status", asString(oldStatus), asString(newStatus), actor));
    }

    public List<AuditEvent> eventsFor(AuditEntityType type, UUID entityId) {
        return auditEventRepository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(type, entityId);
    }

    public List<AuditEvent> recentEvents() {
        return auditEventRepository.findTop50ByOrderByOccurredAtDesc();
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
