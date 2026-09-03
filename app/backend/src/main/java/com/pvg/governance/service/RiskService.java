package com.pvg.governance.service;

import com.pvg.governance.domain.ApplicationEntry;
import com.pvg.governance.domain.AuditEntityType;
import com.pvg.governance.domain.RiskCategory;
import com.pvg.governance.domain.RiskEntry;
import com.pvg.governance.domain.RiskStatus;
import com.pvg.governance.repository.ApplicationEntryRepository;
import com.pvg.governance.repository.RiskEntryRepository;
import com.pvg.governance.api.RiskDtos.RiskRequest;
import com.pvg.governance.api.RiskDtos.RiskResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RiskService {

    private final RiskEntryRepository riskRepository;
    private final ApplicationEntryRepository applicationRepository;
    private final AuditService auditService;

    public RiskService(RiskEntryRepository riskRepository,
                       ApplicationEntryRepository applicationRepository,
                       AuditService auditService) {
        this.riskRepository = riskRepository;
        this.applicationRepository = applicationRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<RiskResponse> findAll(RiskCategory category, RiskStatus status, UUID applicationId) {
        return riskRepository.findAll().stream()
                .filter(risk -> category == null || risk.getCategory() == category)
                .filter(risk -> status == null || risk.getStatus() == status)
                .filter(risk -> applicationId == null || risk.getApplication().getId().equals(applicationId))
                .sorted(Comparator.comparingInt(RiskEntry::residualScore).reversed()
                        .thenComparing(RiskEntry::getTitle, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RiskResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    /**
     * Top-priority view for budget-cycle conversations (proposal §3.1): open entries
     * force-ranked by residual severity score.
     */
    @Transactional(readOnly = true)
    public List<RiskResponse> topPriority(int limit) {
        return riskRepository.findAll().stream()
                .filter(risk -> risk.getStatus() != RiskStatus.CLOSED)
                .sorted(Comparator.comparingInt(RiskEntry::residualScore).reversed()
                        .thenComparing(RiskEntry::getLastAssessedAt))
                .limit(limit)
                .map(this::toResponse)
                .toList();
    }

    /** Staleness tracking (proposal §3.1): entries past their review cadence. */
    @Transactional(readOnly = true)
    public List<RiskResponse> stale() {
        return riskRepository.findPotentiallyStale(LocalDate.now()).stream()
                .filter(RiskEntry::isStale)
                .sorted(Comparator.comparing(RiskEntry::getLastAssessedAt))
                .map(this::toResponse)
                .toList();
    }

    public RiskResponse create(RiskRequest request, String actor) {
        RiskEntry entity = new RiskEntry();
        apply(entity, request);
        RiskEntry saved = riskRepository.save(entity);
        auditService.recordCreated(AuditEntityType.RISK_ENTRY, saved.getId(), saved.getTitle(), actor);
        return toResponse(saved);
    }

    public RiskResponse update(UUID id, RiskRequest request, String actor) {
        RiskEntry entity = getOrThrow(id);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "title", entity.getTitle(), request.title(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "category", entity.getCategory(), request.category(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "description", entity.getDescription(), request.description(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "currentControls", entity.getCurrentControls(), request.currentControls(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "inherentImpact", entity.getInherentImpact(), request.inherentImpact(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "inherentLikelihood", entity.getInherentLikelihood(), request.inherentLikelihood(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "residualImpact", entity.getResidualImpact(), request.residualImpact(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "residualLikelihood", entity.getResidualLikelihood(), request.residualLikelihood(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "blastRadius", entity.getBlastRadius(), request.blastRadius(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "treatmentDecision", entity.getTreatmentDecision(), request.treatmentDecision(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "costToFix", entity.getCostToFix(), request.costToFix(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "targetDate", entity.getTargetDate(), request.targetDate(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "riskOwner", entity.getRiskOwner(), request.riskOwner(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "evidenceLink", entity.getEvidenceLink(), request.evidenceLink(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "reviewCadenceDays", entity.getReviewCadenceDays(), request.reviewCadenceDays(), actor);
        auditService.recordFieldChange(AuditEntityType.RISK_ENTRY, id, "lastAssessedAt", entity.getLastAssessedAt(), request.lastAssessedAt(), actor);
        if (entity.getStatus() != request.status()) {
            auditService.recordStatusChange(AuditEntityType.RISK_ENTRY, id, entity.getStatus(), request.status(), actor);
        }
        apply(entity, request);
        return toResponse(riskRepository.save(entity));
    }

    private void apply(RiskEntry entity, RiskRequest request) {
        ApplicationEntry application = applicationRepository.findById(request.applicationId())
                .orElseThrow(() -> new NotFoundException("Application " + request.applicationId() + " not found"));
        entity.setApplication(application);
        entity.setTitle(request.title());
        entity.setCategory(request.category());
        entity.setDescription(request.description());
        entity.setCurrentControls(request.currentControls());
        entity.setInherentImpact(request.inherentImpact());
        entity.setInherentLikelihood(request.inherentLikelihood());
        entity.setResidualImpact(request.residualImpact());
        entity.setResidualLikelihood(request.residualLikelihood());
        entity.setBlastRadius(request.blastRadius());
        entity.setTreatmentDecision(request.treatmentDecision());
        entity.setCostToFix(request.costToFix());
        entity.setTargetDate(request.targetDate());
        entity.setStatus(request.status());
        entity.setRiskOwner(request.riskOwner());
        entity.setEvidenceLink(request.evidenceLink());
        entity.setReviewCadenceDays(request.reviewCadenceDays());
        entity.setLastAssessedAt(request.lastAssessedAt());
    }

    private RiskEntry getOrThrow(UUID id) {
        return riskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Risk entry " + id + " not found"));
    }

    private RiskResponse toResponse(RiskEntry entity) {
        return new RiskResponse(entity.getId(),
                entity.getApplication().getId(), entity.getApplication().getName(),
                entity.getTitle(), entity.getCategory(), entity.getDescription(), entity.getCurrentControls(),
                entity.getInherentImpact(), entity.getInherentLikelihood(),
                entity.getResidualImpact(), entity.getResidualLikelihood(), entity.residualScore(),
                entity.getBlastRadius(), entity.getTreatmentDecision(), entity.getCostToFix(),
                entity.getTargetDate(), entity.getStatus(), entity.getRiskOwner(), entity.getEvidenceLink(),
                entity.getReviewCadenceDays(), entity.getLastAssessedAt(), entity.isStale(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
