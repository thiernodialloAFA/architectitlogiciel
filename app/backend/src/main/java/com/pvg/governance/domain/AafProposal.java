package com.pvg.governance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A proposal brought to the Architecture Advice Forum (proposal §3.3): submitted
 * with a declared scope and optional links to an ADR draft and/or a landscape
 * entry, scheduled onto a session agenda, then closed with an outcome capture —
 * advice given, by whom, and the final decision taken by the proposer (the forum
 * advises; it does not approve).
 */
@Entity
@Table(name = "aaf_proposal")
public class AafProposal {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AafScope scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProposalStatus status = ProposalStatus.SUBMITTED;

    @Column(name = "submitted_by", nullable = false, length = 200)
    private String submittedBy;

    @Column(length = 200)
    private String department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adr_id")
    private Adr adr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private ApplicationEntry application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ForumSession session;

    @Column(name = "advice_given", columnDefinition = "text")
    private String adviceGiven;

    @Column(name = "advised_by", length = 300)
    private String advisedBy;

    @Column(name = "final_decision", columnDefinition = "text")
    private String finalDecision;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public AafScope getScope() {
        return scope;
    }

    public void setScope(AafScope scope) {
        this.scope = scope;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public void setStatus(ProposalStatus status) {
        this.status = status;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Adr getAdr() {
        return adr;
    }

    public void setAdr(Adr adr) {
        this.adr = adr;
    }

    public ApplicationEntry getApplication() {
        return application;
    }

    public void setApplication(ApplicationEntry application) {
        this.application = application;
    }

    public ForumSession getSession() {
        return session;
    }

    public void setSession(ForumSession session) {
        this.session = session;
    }

    public String getAdviceGiven() {
        return adviceGiven;
    }

    public void setAdviceGiven(String adviceGiven) {
        this.adviceGiven = adviceGiven;
    }

    public String getAdvisedBy() {
        return advisedBy;
    }

    public void setAdvisedBy(String advisedBy) {
        this.advisedBy = advisedBy;
    }

    public String getFinalDecision() {
        return finalDecision;
    }

    public void setFinalDecision(String finalDecision) {
        this.finalDecision = finalDecision;
    }

    public OffsetDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(OffsetDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
