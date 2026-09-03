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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Risk register entry per the Module 4 template (proposal §3.1): category, controls,
 * inherent vs. residual ratings, blast radius, TIME treatment decision, ownership,
 * evidence, and staleness tracking via a review cadence.
 */
@Entity
@Table(name = "risk_entry")
public class RiskEntry {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private ApplicationEntry application;

    @Column(nullable = false, length = 300)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RiskCategory category;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "current_controls", columnDefinition = "text")
    private String currentControls;

    @Enumerated(EnumType.STRING)
    @Column(name = "inherent_impact", nullable = false, length = 10)
    private RatingLevel inherentImpact;

    @Enumerated(EnumType.STRING)
    @Column(name = "inherent_likelihood", nullable = false, length = 10)
    private RatingLevel inherentLikelihood;

    @Enumerated(EnumType.STRING)
    @Column(name = "residual_impact", nullable = false, length = 10)
    private RatingLevel residualImpact;

    @Enumerated(EnumType.STRING)
    @Column(name = "residual_likelihood", nullable = false, length = 10)
    private RatingLevel residualLikelihood;

    @Column(name = "blast_radius", columnDefinition = "text")
    private String blastRadius;

    @Enumerated(EnumType.STRING)
    @Column(name = "treatment_decision", nullable = false, length = 20)
    private TreatmentDecision treatmentDecision;

    @Column(name = "cost_to_fix", length = 100)
    private String costToFix;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskStatus status;

    @Column(name = "risk_owner", nullable = false, length = 200)
    private String riskOwner;

    @Column(name = "evidence_link", columnDefinition = "text")
    private String evidenceLink;

    @Column(name = "review_cadence_days", nullable = false)
    private int reviewCadenceDays = 90;

    @Column(name = "last_assessed_at", nullable = false)
    private LocalDate lastAssessedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    /** Residual severity score (impact x likelihood, 1..9) used for the top-priority view. */
    public int residualScore() {
        return residualImpact.score() * residualLikelihood.score();
    }

    /** An entry is stale when it has not been re-assessed within its review cadence. */
    public boolean isStale() {
        return lastAssessedAt.plusDays(reviewCadenceDays).isBefore(LocalDate.now());
    }

    public UUID getId() {
        return id;
    }

    public ApplicationEntry getApplication() {
        return application;
    }

    public void setApplication(ApplicationEntry application) {
        this.application = application;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RiskCategory getCategory() {
        return category;
    }

    public void setCategory(RiskCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrentControls() {
        return currentControls;
    }

    public void setCurrentControls(String currentControls) {
        this.currentControls = currentControls;
    }

    public RatingLevel getInherentImpact() {
        return inherentImpact;
    }

    public void setInherentImpact(RatingLevel inherentImpact) {
        this.inherentImpact = inherentImpact;
    }

    public RatingLevel getInherentLikelihood() {
        return inherentLikelihood;
    }

    public void setInherentLikelihood(RatingLevel inherentLikelihood) {
        this.inherentLikelihood = inherentLikelihood;
    }

    public RatingLevel getResidualImpact() {
        return residualImpact;
    }

    public void setResidualImpact(RatingLevel residualImpact) {
        this.residualImpact = residualImpact;
    }

    public RatingLevel getResidualLikelihood() {
        return residualLikelihood;
    }

    public void setResidualLikelihood(RatingLevel residualLikelihood) {
        this.residualLikelihood = residualLikelihood;
    }

    public String getBlastRadius() {
        return blastRadius;
    }

    public void setBlastRadius(String blastRadius) {
        this.blastRadius = blastRadius;
    }

    public TreatmentDecision getTreatmentDecision() {
        return treatmentDecision;
    }

    public void setTreatmentDecision(TreatmentDecision treatmentDecision) {
        this.treatmentDecision = treatmentDecision;
    }

    public String getCostToFix() {
        return costToFix;
    }

    public void setCostToFix(String costToFix) {
        this.costToFix = costToFix;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public RiskStatus getStatus() {
        return status;
    }

    public void setStatus(RiskStatus status) {
        this.status = status;
    }

    public String getRiskOwner() {
        return riskOwner;
    }

    public void setRiskOwner(String riskOwner) {
        this.riskOwner = riskOwner;
    }

    public String getEvidenceLink() {
        return evidenceLink;
    }

    public void setEvidenceLink(String evidenceLink) {
        this.evidenceLink = evidenceLink;
    }

    public int getReviewCadenceDays() {
        return reviewCadenceDays;
    }

    public void setReviewCadenceDays(int reviewCadenceDays) {
        this.reviewCadenceDays = reviewCadenceDays;
    }

    public LocalDate getLastAssessedAt() {
        return lastAssessedAt;
    }

    public void setLastAssessedAt(LocalDate lastAssessedAt) {
        this.lastAssessedAt = lastAssessedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
