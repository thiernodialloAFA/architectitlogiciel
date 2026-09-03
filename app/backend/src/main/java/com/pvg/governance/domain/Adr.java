package com.pvg.governance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Architecture Decision Record (Nygard/MADR-style) with a status lifecycle,
 * supersedes/superseded-by cross-links, tags, and links to the risk-register
 * entries it touches (proposal §3.2). AI-integration decisions are flagged with
 * {@code aiRelated} and surface in the AI Architecture Register view (§3.6).
 */
@Entity
@Table(name = "adr")
public class Adr {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "adr_number", nullable = false, unique = true)
    private int adrNumber;

    @Column(nullable = false, length = 300)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdrStatus status;

    @Column(nullable = false, columnDefinition = "text")
    private String context;

    @Column(nullable = false, columnDefinition = "text")
    private String decision;

    @Column(columnDefinition = "text")
    private String consequences;

    @Column(name = "alternatives_considered", columnDefinition = "text")
    private String alternativesConsidered;

    @Column(nullable = false, length = 200)
    private String author;

    @Column(length = 200)
    private String department;

    /** Comma-separated tag list (kept simple for v1; searchable via full-text index). */
    @Column(columnDefinition = "text")
    private String tags;

    @Column(name = "ai_related", nullable = false)
    private boolean aiRelated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdrSource source = AdrSource.PLATFORM;

    @Column(name = "source_repo_url", columnDefinition = "text")
    private String sourceRepoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supersedes_id")
    private Adr supersedes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "superseded_by_id")
    private Adr supersededBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "adr_risk_link",
            joinColumns = @JoinColumn(name = "adr_id"),
            inverseJoinColumns = @JoinColumn(name = "risk_entry_id"))
    private Set<RiskEntry> linkedRisks = new LinkedHashSet<>();

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

    public int getAdrNumber() {
        return adrNumber;
    }

    public void setAdrNumber(int adrNumber) {
        this.adrNumber = adrNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AdrStatus getStatus() {
        return status;
    }

    public void setStatus(AdrStatus status) {
        this.status = status;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getConsequences() {
        return consequences;
    }

    public void setConsequences(String consequences) {
        this.consequences = consequences;
    }

    public String getAlternativesConsidered() {
        return alternativesConsidered;
    }

    public void setAlternativesConsidered(String alternativesConsidered) {
        this.alternativesConsidered = alternativesConsidered;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public boolean isAiRelated() {
        return aiRelated;
    }

    public void setAiRelated(boolean aiRelated) {
        this.aiRelated = aiRelated;
    }

    public AdrSource getSource() {
        return source;
    }

    public void setSource(AdrSource source) {
        this.source = source;
    }

    public String getSourceRepoUrl() {
        return sourceRepoUrl;
    }

    public void setSourceRepoUrl(String sourceRepoUrl) {
        this.sourceRepoUrl = sourceRepoUrl;
    }

    public Adr getSupersedes() {
        return supersedes;
    }

    public void setSupersedes(Adr supersedes) {
        this.supersedes = supersedes;
    }

    public Adr getSupersededBy() {
        return supersededBy;
    }

    public void setSupersededBy(Adr supersededBy) {
        this.supersededBy = supersededBy;
    }

    public Set<RiskEntry> getLinkedRisks() {
        return linkedRisks;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
