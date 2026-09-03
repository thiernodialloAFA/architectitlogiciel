package com.pvg.governance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * The explicit, rarely-used arbitration record for group-scope escalations
 * (proposal §3.3). Immutable once written — like the audit trail, an escalation
 * is a matter of record, so there are intentionally no setters and no update or
 * delete endpoint; a healthy advice process should show few of these.
 */
@Entity
@Table(name = "arbitration_record")
public class ArbitrationRecord {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposal_id", nullable = false, unique = true)
    private AafProposal proposal;

    @Column(name = "requested_by", nullable = false, length = 200)
    private String requestedBy;

    @Column(nullable = false, columnDefinition = "text")
    private String rationale;

    @Column(nullable = false, columnDefinition = "text")
    private String outcome;

    @Column(name = "decided_by", nullable = false, length = 200)
    private String decidedBy;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt = OffsetDateTime.now();

    protected ArbitrationRecord() {
        // for JPA
    }

    public ArbitrationRecord(AafProposal proposal, String requestedBy, String rationale,
                             String outcome, String decidedBy) {
        this.proposal = proposal;
        this.requestedBy = requestedBy;
        this.rationale = rationale;
        this.outcome = outcome;
        this.decidedBy = decidedBy;
    }

    public UUID getId() {
        return id;
    }

    public AafProposal getProposal() {
        return proposal;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public String getRationale() {
        return rationale;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getDecidedBy() {
        return decidedBy;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
