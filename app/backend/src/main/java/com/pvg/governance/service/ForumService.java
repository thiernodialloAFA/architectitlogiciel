package com.pvg.governance.service;

import com.pvg.governance.api.ForumDtos.AdrRefLite;
import com.pvg.governance.api.ForumDtos.ApplicationRefLite;
import com.pvg.governance.api.ForumDtos.ArbitrationRequest;
import com.pvg.governance.api.ForumDtos.ArbitrationResponse;
import com.pvg.governance.api.ForumDtos.ProposalCreateRequest;
import com.pvg.governance.api.ForumDtos.ProposalOutcomeRequest;
import com.pvg.governance.api.ForumDtos.ProposalResponse;
import com.pvg.governance.api.ForumDtos.ProposalScheduleRequest;
import com.pvg.governance.api.ForumDtos.SessionCreateRequest;
import com.pvg.governance.api.ForumDtos.SessionDetailResponse;
import com.pvg.governance.api.ForumDtos.SessionHoldRequest;
import com.pvg.governance.api.ForumDtos.SessionRefLite;
import com.pvg.governance.api.ForumDtos.SessionResponse;
import com.pvg.governance.domain.AafProposal;
import com.pvg.governance.domain.AafScope;
import com.pvg.governance.domain.Adr;
import com.pvg.governance.domain.ApplicationEntry;
import com.pvg.governance.domain.ArbitrationRecord;
import com.pvg.governance.domain.AuditEntityType;
import com.pvg.governance.domain.ForumSession;
import com.pvg.governance.domain.ForumSessionStatus;
import com.pvg.governance.domain.ProposalStatus;
import com.pvg.governance.repository.AafProposalRepository;
import com.pvg.governance.repository.AdrRepository;
import com.pvg.governance.repository.ApplicationEntryRepository;
import com.pvg.governance.repository.ArbitrationRecordRepository;
import com.pvg.governance.repository.ForumSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Architecture Advice Forum & governance workflow (proposal §3.3). The workflow
 * mirrors the advice process: proposals are submitted with a declared scope,
 * scheduled onto a forum session's agenda, and closed with an outcome capture in
 * which the proposer records the advice received and their own final decision —
 * the forum advises, it never approves. Group-scope proposals may additionally
 * be escalated to an explicit, immutable arbitration record.
 */
@Service
@Transactional
public class ForumService {

    private final ForumSessionRepository sessionRepository;
    private final AafProposalRepository proposalRepository;
    private final ArbitrationRecordRepository arbitrationRepository;
    private final AdrRepository adrRepository;
    private final ApplicationEntryRepository applicationRepository;
    private final AuditService auditService;

    public ForumService(ForumSessionRepository sessionRepository,
                        AafProposalRepository proposalRepository,
                        ArbitrationRecordRepository arbitrationRepository,
                        AdrRepository adrRepository,
                        ApplicationEntryRepository applicationRepository,
                        AuditService auditService) {
        this.sessionRepository = sessionRepository;
        this.proposalRepository = proposalRepository;
        this.arbitrationRepository = arbitrationRepository;
        this.adrRepository = adrRepository;
        this.applicationRepository = applicationRepository;
        this.auditService = auditService;
    }

    // ------------------------------------------------------------------ sessions

    @Transactional(readOnly = true)
    public List<SessionResponse> findAllSessions() {
        return sessionRepository.findAllByOrderBySessionDateDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionDetailResponse sessionDetail(UUID id) {
        ForumSession session = getSessionOrThrow(id);
        List<ProposalResponse> agenda = proposalRepository.findBySessionIdOrderByCreatedAtAsc(id).stream()
                .map(this::toResponse)
                .toList();
        return new SessionDetailResponse(toResponse(session), agenda);
    }

    public SessionResponse createSession(SessionCreateRequest request, String actor) {
        ForumSession session = new ForumSession();
        session.setSessionDate(request.sessionDate());
        session.setTitle(request.title());
        session.setNotes(request.notes());
        ForumSession saved = sessionRepository.save(session);
        auditService.recordCreated(AuditEntityType.FORUM_SESSION, saved.getId(), saved.getTitle(), actor);
        return toResponse(saved);
    }

    public SessionResponse holdSession(UUID id, SessionHoldRequest request, String actor) {
        ForumSession session = getSessionOrThrow(id);
        if (session.getStatus() != ForumSessionStatus.PLANNED) {
            throw new IllegalStateException("Forum session '" + session.getTitle() + "' has already been held");
        }
        auditService.recordStatusChange(AuditEntityType.FORUM_SESSION, id,
                session.getStatus(), ForumSessionStatus.HELD, actor);
        if (request != null && request.notes() != null && !request.notes().isBlank()) {
            auditService.recordFieldChange(AuditEntityType.FORUM_SESSION, id, "notes",
                    session.getNotes(), request.notes(), actor);
            session.setNotes(request.notes());
        }
        session.setStatus(ForumSessionStatus.HELD);
        return toResponse(sessionRepository.save(session));
    }

    // ----------------------------------------------------------------- proposals

    @Transactional(readOnly = true)
    public List<ProposalResponse> findAllProposals(ProposalStatus status) {
        List<AafProposal> proposals = status == null
                ? proposalRepository.findAllByOrderByCreatedAtDesc()
                : proposalRepository.findByStatusOrderByCreatedAtDesc(status);
        return proposals.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProposalResponse findProposalById(UUID id) {
        return toResponse(getProposalOrThrow(id));
    }

    public ProposalResponse createProposal(ProposalCreateRequest request, String actor) {
        AafProposal proposal = new AafProposal();
        proposal.setTitle(request.title());
        proposal.setSummary(request.summary());
        proposal.setScope(request.scope());
        proposal.setSubmittedBy(request.submittedBy());
        proposal.setDepartment(request.department());
        if (request.adrId() != null) {
            proposal.setAdr(adrRepository.findById(request.adrId())
                    .orElseThrow(() -> new NotFoundException("ADR " + request.adrId() + " not found")));
        }
        if (request.applicationId() != null) {
            proposal.setApplication(applicationRepository.findById(request.applicationId())
                    .orElseThrow(() -> new NotFoundException("Application " + request.applicationId() + " not found")));
        }
        AafProposal saved = proposalRepository.save(proposal);
        auditService.recordCreated(AuditEntityType.AAF_PROPOSAL, saved.getId(), saved.getTitle(), actor);
        return toResponse(saved);
    }

    public ProposalResponse schedule(UUID id, ProposalScheduleRequest request, String actor) {
        AafProposal proposal = getProposalOrThrow(id);
        if (proposal.getStatus() != ProposalStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED proposals can be scheduled (current status: "
                    + proposal.getStatus() + ")");
        }
        ForumSession session = getSessionOrThrow(request.sessionId());
        if (session.getStatus() != ForumSessionStatus.PLANNED) {
            throw new IllegalStateException("Cannot schedule onto '" + session.getTitle()
                    + "': the session has already been held");
        }
        proposal.setSession(session);
        auditService.recordStatusChange(AuditEntityType.AAF_PROPOSAL, id,
                proposal.getStatus(), ProposalStatus.SCHEDULED, actor);
        auditService.recordFieldChange(AuditEntityType.AAF_PROPOSAL, id, "session",
                null, session.getTitle(), actor);
        proposal.setStatus(ProposalStatus.SCHEDULED);
        return toResponse(proposalRepository.save(proposal));
    }

    public ProposalResponse recordOutcome(UUID id, ProposalOutcomeRequest request, String actor) {
        AafProposal proposal = getProposalOrThrow(id);
        if (proposal.getStatus() != ProposalStatus.SCHEDULED) {
            throw new IllegalStateException("Outcome can only be captured for a SCHEDULED proposal (current status: "
                    + proposal.getStatus() + ")");
        }
        proposal.setAdviceGiven(request.adviceGiven());
        proposal.setAdvisedBy(request.advisedBy());
        proposal.setFinalDecision(request.finalDecision());
        proposal.setDecidedAt(OffsetDateTime.now());
        if (request.resultingAdrId() != null) {
            proposal.setAdr(adrRepository.findById(request.resultingAdrId())
                    .orElseThrow(() -> new NotFoundException("ADR " + request.resultingAdrId() + " not found")));
        }
        auditService.recordStatusChange(AuditEntityType.AAF_PROPOSAL, id,
                proposal.getStatus(), ProposalStatus.DECIDED, actor);
        auditService.recordFieldChange(AuditEntityType.AAF_PROPOSAL, id, "finalDecision",
                null, request.finalDecision(), actor);
        proposal.setStatus(ProposalStatus.DECIDED);
        return toResponse(proposalRepository.save(proposal));
    }

    // --------------------------------------------------------------- arbitration

    @Transactional(readOnly = true)
    public List<ArbitrationResponse> findAllArbitrations() {
        return arbitrationRepository.findAllByOrderByOccurredAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public ArbitrationResponse arbitrate(UUID proposalId, ArbitrationRequest request, String actor) {
        AafProposal proposal = getProposalOrThrow(proposalId);
        if (proposal.getScope() != AafScope.GROUP) {
            throw new IllegalStateException(
                    "Arbitration is the group-scope escalation path only, never a default — this proposal is scoped "
                            + proposal.getScope());
        }
        if (proposal.getStatus() == ProposalStatus.DECIDED) {
            throw new IllegalStateException("Proposal '" + proposal.getTitle() + "' is already decided");
        }
        arbitrationRepository.findByProposalId(proposalId).ifPresent(existing -> {
            throw new IllegalStateException("An arbitration record already exists for this proposal");
        });
        ArbitrationRecord record = arbitrationRepository.save(new ArbitrationRecord(
                proposal, request.requestedBy(), request.rationale(), request.outcome(), request.decidedBy()));
        auditService.recordCreated(AuditEntityType.ARBITRATION, record.getId(),
                "Arbitration of '" + proposal.getTitle() + "': " + request.outcome(), actor);
        auditService.recordStatusChange(AuditEntityType.AAF_PROPOSAL, proposalId,
                proposal.getStatus(), ProposalStatus.DECIDED, actor);
        proposal.setFinalDecision("Decided by group-scope arbitration: " + request.outcome());
        proposal.setDecidedAt(OffsetDateTime.now());
        proposal.setStatus(ProposalStatus.DECIDED);
        proposalRepository.save(proposal);
        return toResponse(record);
    }

    // ------------------------------------------------------------------- mapping

    private ForumSession getSessionOrThrow(UUID id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Forum session " + id + " not found"));
    }

    private AafProposal getProposalOrThrow(UUID id) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("AAF proposal " + id + " not found"));
    }

    private SessionResponse toResponse(ForumSession session) {
        int agendaSize = proposalRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).size();
        return new SessionResponse(session.getId(), session.getSessionDate(), session.getTitle(),
                session.getStatus(), session.getNotes(), agendaSize,
                session.getCreatedAt(), session.getUpdatedAt());
    }

    private ProposalResponse toResponse(AafProposal proposal) {
        Adr adr = proposal.getAdr();
        ApplicationEntry application = proposal.getApplication();
        ForumSession session = proposal.getSession();
        boolean arbitrated = arbitrationRepository.findByProposalId(proposal.getId()).isPresent();
        return new ProposalResponse(proposal.getId(), proposal.getTitle(), proposal.getSummary(),
                proposal.getScope(), proposal.getStatus(), proposal.getSubmittedBy(), proposal.getDepartment(),
                adr == null ? null : new AdrRefLite(adr.getId(), adr.getAdrNumber(), adr.getTitle()),
                application == null ? null : new ApplicationRefLite(application.getId(), application.getName()),
                session == null ? null : new SessionRefLite(session.getId(), session.getSessionDate(), session.getTitle()),
                proposal.getAdviceGiven(), proposal.getAdvisedBy(), proposal.getFinalDecision(),
                proposal.getDecidedAt(), arbitrated, proposal.getCreatedAt(), proposal.getUpdatedAt());
    }

    private ArbitrationResponse toResponse(ArbitrationRecord record) {
        return new ArbitrationResponse(record.getId(), record.getProposal().getId(),
                record.getProposal().getTitle(), record.getRequestedBy(), record.getRationale(),
                record.getOutcome(), record.getDecidedBy(), record.getOccurredAt());
    }
}
