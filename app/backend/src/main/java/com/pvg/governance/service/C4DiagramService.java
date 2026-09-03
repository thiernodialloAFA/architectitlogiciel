package com.pvg.governance.service;

import com.pvg.governance.api.C4Dtos.C4DiagramCreateRequest;
import com.pvg.governance.api.C4Dtos.C4DiagramResponse;
import com.pvg.governance.domain.ApplicationEntry;
import com.pvg.governance.domain.AuditEntityType;
import com.pvg.governance.domain.C4Diagram;
import com.pvg.governance.repository.ApplicationEntryRepository;
import com.pvg.governance.repository.C4DiagramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * C4 model viewer backend (proposal §3.5): textual C4 (Mermaid syntax) stored per
 * landscape entry, versioned alongside it. Versions are append-only — a revision
 * inserts the next version; nothing is updated or deleted — and the diagram
 * source is stored verbatim and treated as untrusted input by the renderer.
 */
@Service
@Transactional
public class C4DiagramService {

    private final C4DiagramRepository diagramRepository;
    private final ApplicationEntryRepository applicationRepository;
    private final AuditService auditService;

    public C4DiagramService(C4DiagramRepository diagramRepository,
                            ApplicationEntryRepository applicationRepository,
                            AuditService auditService) {
        this.diagramRepository = diagramRepository;
        this.applicationRepository = applicationRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<C4DiagramResponse> versionsFor(UUID applicationId) {
        getApplicationOrThrow(applicationId);
        return diagramRepository.findByApplicationIdOrderByVersionDesc(applicationId).stream()
                .map(this::toResponse)
                .toList();
    }

    public C4DiagramResponse addVersion(UUID applicationId, C4DiagramCreateRequest request, String actor) {
        ApplicationEntry application = getApplicationOrThrow(applicationId);
        int nextVersion = diagramRepository.findFirstByApplicationIdOrderByVersionDesc(applicationId)
                .map(latest -> latest.getVersion() + 1)
                .orElse(1);
        C4Diagram saved = diagramRepository.save(
                new C4Diagram(application, nextVersion, request.label(), request.source(), actor));
        auditService.recordCreated(AuditEntityType.C4_DIAGRAM, saved.getId(),
                saved.getLabel() + " (v" + saved.getVersion() + ") for " + application.getName(), actor);
        return toResponse(saved);
    }

    private ApplicationEntry getApplicationOrThrow(UUID id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application " + id + " not found"));
    }

    private C4DiagramResponse toResponse(C4Diagram diagram) {
        return new C4DiagramResponse(diagram.getId(), diagram.getApplication().getId(),
                diagram.getVersion(), diagram.getLabel(), diagram.getSource(),
                diagram.getAuthor(), diagram.getCreatedAt());
    }
}
