package com.pvg.governance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * One immutable version of a textual C4 diagram for a landscape entry (proposal
 * §3.5). Versions are append-only: saving a revision inserts a new row with the
 * next version number; existing versions are never updated or deleted. The
 * source is Mermaid C4 syntax, rendered client-side by a pinned Mermaid build
 * and treated as untrusted input for rendering purposes (§7).
 */
@Entity
@Table(name = "c4_diagram")
public class C4Diagram {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private ApplicationEntry application;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, length = 200)
    private String label;

    @Column(nullable = false, columnDefinition = "text")
    private String source;

    @Column(nullable = false, length = 200)
    private String author;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected C4Diagram() {
        // for JPA
    }

    public C4Diagram(ApplicationEntry application, int version, String label, String source, String author) {
        this.application = application;
        this.version = version;
        this.label = label;
        this.source = source;
        this.author = author;
    }

    public UUID getId() {
        return id;
    }

    public ApplicationEntry getApplication() {
        return application;
    }

    public int getVersion() {
        return version;
    }

    public String getLabel() {
        return label;
    }

    public String getSource() {
        return source;
    }

    public String getAuthor() {
        return author;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
