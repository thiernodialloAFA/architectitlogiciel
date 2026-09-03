package com.pvg.governance.service;

import com.pvg.governance.domain.Adr;
import com.pvg.governance.domain.AdrSource;
import com.pvg.governance.domain.AdrStatus;
import com.pvg.governance.domain.AuditEntityType;
import com.pvg.governance.domain.RiskEntry;
import com.pvg.governance.repository.AdrRepository;
import com.pvg.governance.repository.RiskEntryRepository;
import com.pvg.governance.api.AdrDtos.AdrCreateRequest;
import com.pvg.governance.api.AdrDtos.AdrRef;
import com.pvg.governance.api.AdrDtos.AdrResponse;
import com.pvg.governance.api.AdrDtos.AdrStatusChangeRequest;
import com.pvg.governance.api.AdrDtos.AdrUpdateRequest;
import com.pvg.governance.api.AdrDtos.LinkedRiskRef;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class AdrService {

    /** Allowed status lifecycle: Proposed → Accepted → Deprecated/Superseded (proposal §3.2). */
    private static final Set<AdrStatus> TERMINAL_STATUSES = Set.of(AdrStatus.DEPRECATED, AdrStatus.SUPERSEDED);

    private final AdrRepository adrRepository;
    private final RiskEntryRepository riskRepository;
    private final AuditService auditService;

    public AdrService(AdrRepository adrRepository,
                      RiskEntryRepository riskRepository,
                      AuditService auditService) {
        this.adrRepository = adrRepository;
        this.riskRepository = riskRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<AdrResponse> findAll(boolean aiOnly) {
        List<Adr> adrs = aiOnly
                ? adrRepository.findByAiRelatedTrueOrderByAdrNumberAsc()
                : adrRepository.findAllByOrderByAdrNumberAsc();
        return adrs.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdrResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<AdrResponse> search(String query) {
        return adrRepository.searchFullText(query).stream().map(this::toResponse).toList();
    }

    public AdrResponse create(AdrCreateRequest request, String actor) {
        Adr adr = new Adr();
        adr.setAdrNumber(adrRepository.nextAdrNumber());
        adr.setStatus(AdrStatus.PROPOSED);
        adr.setTitle(request.title());
        adr.setContext(request.context());
        adr.setDecision(request.decision());
        adr.setConsequences(request.consequences());
        adr.setAlternativesConsidered(request.alternativesConsidered());
        adr.setAuthor(request.author());
        adr.setDepartment(request.department());
        adr.setTags(request.tags());
        adr.setAiRelated(request.aiRelated());
        linkRisks(adr, request.linkedRiskIds());
        Adr saved = adrRepository.save(adr);
        auditService.recordCreated(AuditEntityType.ADR, saved.getId(),
                "ADR-" + saved.getAdrNumber() + ": " + saved.getTitle(), actor);
        return toResponse(saved);
    }

    public AdrResponse update(UUID id, AdrUpdateRequest request, String actor) {
        Adr adr = getOrThrow(id);
        requirePlatformOwned(adr);
        if (TERMINAL_STATUSES.contains(adr.getStatus())) {
            throw new IllegalStateException(
                    "ADR-" + adr.getAdrNumber() + " is " + adr.getStatus() + " and can no longer be edited");
        }
        auditService.recordFieldChange(AuditEntityType.ADR, id, "title", adr.getTitle(), request.title(), actor);
        auditService.recordFieldChange(AuditEntityType.ADR, id, "context", adr.getContext(), request.context(), actor);
        auditService.recordFieldChange(AuditEntityType.ADR, id, "decision", adr.getDecision(), request.decision(), actor);
        auditService.recordFieldChange(AuditEntityType.ADR, id, "consequences", adr.getConsequences(), request.consequences(), actor);
        auditService.recordFieldChange(AuditEntityType.ADR, id, "alternativesConsidered", adr.getAlternativesConsidered(), request.alternativesConsidered(), actor);
        auditService.recordFieldChange(AuditEntityType.ADR, id, "department", adr.getDepartment(), request.department(), actor);
        auditService.recordFieldChange(AuditEntityType.ADR, id, "tags", adr.getTags(), request.tags(), actor);
        auditService.recordFieldChange(AuditEntityType.ADR, id, "aiRelated", adr.isAiRelated(), request.aiRelated(), actor);
        adr.setTitle(request.title());
        adr.setContext(request.context());
        adr.setDecision(request.decision());
        adr.setConsequences(request.consequences());
        adr.setAlternativesConsidered(request.alternativesConsidered());
        adr.setDepartment(request.department());
        adr.setTags(request.tags());
        adr.setAiRelated(request.aiRelated());
        linkRisks(adr, request.linkedRiskIds());
        return toResponse(adrRepository.save(adr));
    }

    public AdrResponse changeStatus(UUID id, AdrStatusChangeRequest request, String actor) {
        Adr adr = getOrThrow(id);
        requirePlatformOwned(adr);
        AdrStatus from = adr.getStatus();
        AdrStatus to = request.status();
        if (!isAllowedTransition(from, to)) {
            throw new IllegalStateException("Invalid ADR status transition: " + from + " → " + to);
        }
        if (to == AdrStatus.SUPERSEDED) {
            if (request.supersededById() == null) {
                throw new IllegalStateException("A superseding ADR must be provided when marking an ADR as SUPERSEDED");
            }
            Adr supersededBy = getOrThrow(request.supersededById());
            if (supersededBy.getId().equals(adr.getId())) {
                throw new IllegalStateException("An ADR cannot supersede itself");
            }
            adr.setSupersededBy(supersededBy);
            supersededBy.setSupersedes(adr);
            adrRepository.save(supersededBy);
        }
        adr.setStatus(to);
        auditService.recordStatusChange(AuditEntityType.ADR, id, from, to, actor);
        return toResponse(adrRepository.save(adr));
    }

    private static boolean isAllowedTransition(AdrStatus from, AdrStatus to) {
        return switch (from) {
            case PROPOSED -> to == AdrStatus.ACCEPTED;
            case ACCEPTED -> to == AdrStatus.DEPRECATED || to == AdrStatus.SUPERSEDED;
            case DEPRECATED, SUPERSEDED -> false;
        };
    }

    /**
     * Repository-indexed ADRs (§3.2 option a) are read-only copies: the canonical
     * record lives in the owning team's repository and changes arrive via re-import,
     * never through platform editing — that would create a second source of truth.
     */
    private static void requirePlatformOwned(Adr adr) {
        if (adr.getSource() == AdrSource.REPOSITORY) {
            throw new IllegalStateException("ADR-" + adr.getAdrNumber()
                    + " is indexed read-only from " + adr.getSourceRepoUrl()
                    + "; edit it in the owning repository and re-import");
        }
    }

    private void linkRisks(Adr adr, List<UUID> riskIds) {
        adr.getLinkedRisks().clear();
        if (riskIds == null) {
            return;
        }
        for (UUID riskId : riskIds) {
            RiskEntry risk = riskRepository.findById(riskId)
                    .orElseThrow(() -> new NotFoundException("Risk entry " + riskId + " not found"));
            adr.getLinkedRisks().add(risk);
        }
    }

    private Adr getOrThrow(UUID id) {
        return adrRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ADR " + id + " not found"));
    }

    private AdrResponse toResponse(Adr adr) {
        return new AdrResponse(adr.getId(), adr.getAdrNumber(), adr.getTitle(), adr.getStatus(),
                adr.getContext(), adr.getDecision(), adr.getConsequences(), adr.getAlternativesConsidered(),
                adr.getAuthor(), adr.getDepartment(), adr.getTags(), adr.isAiRelated(),
                adr.getSource(), adr.getSourceRepoUrl(), adr.getSourcePath(),
                toRef(adr.getSupersedes()), toRef(adr.getSupersededBy()),
                adr.getLinkedRisks().stream()
                        .map(risk -> new LinkedRiskRef(risk.getId(), risk.getTitle()))
                        .toList(),
                adr.getCreatedAt(), adr.getUpdatedAt());
    }

    private static AdrRef toRef(Adr adr) {
        return adr == null ? null : new AdrRef(adr.getId(), adr.getAdrNumber(), adr.getTitle());
    }
}
