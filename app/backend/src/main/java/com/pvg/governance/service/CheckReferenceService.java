package com.pvg.governance.service;

import com.pvg.governance.api.CheckDtos.CheckCreateRequest;
import com.pvg.governance.api.CheckDtos.CheckResponse;
import com.pvg.governance.api.CheckDtos.CheckStatusUpdateRequest;
import com.pvg.governance.domain.ApplicationEntry;
import com.pvg.governance.domain.AuditEntityType;
import com.pvg.governance.domain.CheckReference;
import com.pvg.governance.repository.ApplicationEntryRepository;
import com.pvg.governance.repository.CheckReferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Fitness-function / contract-testing status integration (proposal §2, §6 item 6):
 * the platform records that a check exists on a landscape entry and its latest
 * pass/fail status, pushed by the owning team's CI through the status endpoint
 * (or set manually as the link-based first pass). Every status change is audited,
 * so the pass/fail history is queryable without reimplementing any CI tool.
 */
@Service
@Transactional
public class CheckReferenceService {

    private final CheckReferenceRepository checkRepository;
    private final ApplicationEntryRepository applicationRepository;
    private final AuditService auditService;

    public CheckReferenceService(CheckReferenceRepository checkRepository,
                                 ApplicationEntryRepository applicationRepository,
                                 AuditService auditService) {
        this.checkRepository = checkRepository;
        this.applicationRepository = applicationRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<CheckResponse> checksFor(UUID applicationId) {
        getApplicationOrThrow(applicationId);
        return checkRepository.findByApplicationIdOrderByNameAsc(applicationId).stream()
                .map(this::toResponse)
                .toList();
    }

    public CheckResponse create(UUID applicationId, CheckCreateRequest request, String actor) {
        ApplicationEntry application = getApplicationOrThrow(applicationId);
        CheckReference check = new CheckReference();
        check.setApplication(application);
        check.setName(request.name());
        check.setCheckType(request.checkType());
        check.setTool(request.tool());
        check.setLink(request.link());
        check.setDescription(request.description());
        CheckReference saved = checkRepository.save(check);
        auditService.recordCreated(AuditEntityType.CHECK_REFERENCE, saved.getId(),
                saved.getName() + " (" + saved.getTool() + ") on " + application.getName(), actor);
        return toResponse(saved);
    }

    public CheckResponse updateStatus(UUID checkId, CheckStatusUpdateRequest request, String actor) {
        CheckReference check = checkRepository.findById(checkId)
                .orElseThrow(() -> new NotFoundException("Check reference " + checkId + " not found"));
        auditService.recordStatusChange(AuditEntityType.CHECK_REFERENCE, checkId,
                check.getLastStatus(), request.status(), actor);
        check.setLastStatus(request.status());
        check.setLastRunAt(request.runAt() != null ? request.runAt() : OffsetDateTime.now());
        return toResponse(checkRepository.save(check));
    }

    private ApplicationEntry getApplicationOrThrow(UUID id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application " + id + " not found"));
    }

    private CheckResponse toResponse(CheckReference check) {
        return new CheckResponse(check.getId(), check.getApplication().getId(),
                check.getApplication().getName(), check.getName(), check.getCheckType(),
                check.getTool(), check.getLink(), check.getDescription(),
                check.getLastStatus(), check.getLastRunAt(), check.getCreatedAt(), check.getUpdatedAt());
    }
}
