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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A curated, versioned standard / reference pattern (proposal §3.4), linked to
 * the ADRs that established or amended it and to the landscape entries where it
 * is applied (adoption visibility). Content changes bump the version; the
 * append-only audit trail keeps each version's old/new content.
 */
@Entity
@Table(name = "standard")
public class Standard {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 300)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StandardCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StandardStatus status = StandardStatus.DRAFT;

    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false, length = 200)
    private String owner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "standard_adr_link",
            joinColumns = @JoinColumn(name = "standard_id"),
            inverseJoinColumns = @JoinColumn(name = "adr_id"))
    private Set<Adr> linkedAdrs = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "standard_application_link",
            joinColumns = @JoinColumn(name = "standard_id"),
            inverseJoinColumns = @JoinColumn(name = "application_id"))
    private Set<ApplicationEntry> appliedApplications = new LinkedHashSet<>();

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

    public StandardCategory getCategory() {
        return category;
    }

    public void setCategory(StandardCategory category) {
        this.category = category;
    }

    public StandardStatus getStatus() {
        return status;
    }

    public void setStatus(StandardStatus status) {
        this.status = status;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Set<Adr> getLinkedAdrs() {
        return linkedAdrs;
    }

    public Set<ApplicationEntry> getAppliedApplications() {
        return appliedApplications;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
