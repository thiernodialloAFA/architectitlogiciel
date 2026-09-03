package com.pvg.governance.service;

import com.pvg.governance.api.ForumDtos.AdrRefLite;
import com.pvg.governance.api.ForumDtos.ApplicationRefLite;
import com.pvg.governance.api.StandardDtos.StandardRequest;
import com.pvg.governance.api.StandardDtos.StandardResponse;
import com.pvg.governance.api.StandardDtos.StandardStatusChangeRequest;
import com.pvg.governance.domain.AuditEntityType;
import com.pvg.governance.domain.Standard;
import com.pvg.governance.domain.StandardCategory;
import com.pvg.governance.domain.StandardStatus;
import com.pvg.governance.repository.AdrRepository;
import com.pvg.governance.repository.ApplicationEntryRepository;
import com.pvg.governance.repository.StandardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Standards & reference patterns library (proposal §3.4): curated, versioned
 * documents linked to the ADRs that established or amended them and to the
 * landscape entries where they are applied. A content change bumps the version;
 * the audit trail records each bump's old/new content, forming the document's
 * version history. Retired standards are immutable, like terminal ADRs.
 */
@Service
@Transactional
public class StandardService {

    private final StandardRepository standardRepository;
    private final AdrRepository adrRepository;
    private final ApplicationEntryRepository applicationRepository;
    private final AuditService auditService;

    public StandardService(StandardRepository standardRepository,
                           AdrRepository adrRepository,
                           ApplicationEntryRepository applicationRepository,
                           AuditService auditService) {
        this.standardRepository = standardRepository;
        this.adrRepository = adrRepository;
        this.applicationRepository = applicationRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<StandardResponse> findAll(StandardCategory category) {
        List<Standard> standards = category == null
                ? standardRepository.findAllByOrderByTitleAsc()
                : standardRepository.findByCategoryOrderByTitleAsc(category);
        return standards.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StandardResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public StandardResponse create(StandardRequest request, String actor) {
        Standard standard = new Standard();
        standard.setTitle(request.title());
        standard.setCategory(request.category());
        standard.setContent(request.content());
        standard.setOwner(request.owner());
        applyLinks(standard, request);
        Standard saved = standardRepository.save(standard);
        auditService.recordCreated(AuditEntityType.STANDARD, saved.getId(), saved.getTitle(), actor);
        return toResponse(saved);
    }

    public StandardResponse update(UUID id, StandardRequest request, String actor) {
        Standard standard = getOrThrow(id);
        if (standard.getStatus() == StandardStatus.RETIRED) {
            throw new IllegalStateException("'" + standard.getTitle() + "' is retired and can no longer be edited");
        }
        auditService.recordFieldChange(AuditEntityType.STANDARD, id, "title", standard.getTitle(), request.title(), actor);
        auditService.recordFieldChange(AuditEntityType.STANDARD, id, "category", standard.getCategory(), request.category(), actor);
        auditService.recordFieldChange(AuditEntityType.STANDARD, id, "owner", standard.getOwner(), request.owner(), actor);
        if (!Objects.equals(standard.getContent(), request.content())) {
            // Content change = new document version; the audit event holds the old/new text.
            auditService.recordFieldChange(AuditEntityType.STANDARD, id, "content",
                    standard.getContent(), request.content(), actor);
            standard.setVersion(standard.getVersion() + 1);
            auditService.recordFieldChange(AuditEntityType.STANDARD, id, "version",
                    standard.getVersion() - 1, standard.getVersion(), actor);
        }
        standard.setTitle(request.title());
        standard.setCategory(request.category());
        standard.setContent(request.content());
        standard.setOwner(request.owner());
        applyLinks(standard, request);
        return toResponse(standardRepository.save(standard));
    }

    public StandardResponse changeStatus(UUID id, StandardStatusChangeRequest request, String actor) {
        Standard standard = getOrThrow(id);
        StandardStatus from = standard.getStatus();
        StandardStatus to = request.status();
        if (!isAllowedTransition(from, to)) {
            throw new IllegalStateException("Invalid standard status transition: " + from + " → " + to);
        }
        auditService.recordStatusChange(AuditEntityType.STANDARD, id, from, to, actor);
        standard.setStatus(to);
        return toResponse(standardRepository.save(standard));
    }

    private static boolean isAllowedTransition(StandardStatus from, StandardStatus to) {
        return switch (from) {
            case DRAFT -> to == StandardStatus.ACTIVE;
            case ACTIVE -> to == StandardStatus.RETIRED;
            case RETIRED -> false;
        };
    }

    private void applyLinks(Standard standard, StandardRequest request) {
        standard.getLinkedAdrs().clear();
        if (request.linkedAdrIds() != null) {
            for (UUID adrId : request.linkedAdrIds()) {
                standard.getLinkedAdrs().add(adrRepository.findById(adrId)
                        .orElseThrow(() -> new NotFoundException("ADR " + adrId + " not found")));
            }
        }
        standard.getAppliedApplications().clear();
        if (request.appliedApplicationIds() != null) {
            for (UUID applicationId : request.appliedApplicationIds()) {
                standard.getAppliedApplications().add(applicationRepository.findById(applicationId)
                        .orElseThrow(() -> new NotFoundException("Application " + applicationId + " not found")));
            }
        }
    }

    private Standard getOrThrow(UUID id) {
        return standardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Standard " + id + " not found"));
    }

    private StandardResponse toResponse(Standard standard) {
        return new StandardResponse(standard.getId(), standard.getTitle(), standard.getCategory(),
                standard.getStatus(), standard.getVersion(), standard.getContent(), standard.getOwner(),
                standard.getLinkedAdrs().stream()
                        .map(adr -> new AdrRefLite(adr.getId(), adr.getAdrNumber(), adr.getTitle()))
                        .toList(),
                standard.getAppliedApplications().stream()
                        .map(app -> new ApplicationRefLite(app.getId(), app.getName()))
                        .toList(),
                standard.getCreatedAt(), standard.getUpdatedAt());
    }
}
