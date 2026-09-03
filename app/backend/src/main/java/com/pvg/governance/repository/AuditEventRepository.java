package com.pvg.governance.repository;

import com.pvg.governance.domain.AuditEntityType;
import com.pvg.governance.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(AuditEntityType entityType, UUID entityId);

    List<AuditEvent> findTop50ByOrderByOccurredAtDesc();
}
