package com.pvg.governance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/** A directed integration edge between two landscape entries (cross-domain view). */
@Entity
@Table(name = "application_dependency")
public class ApplicationDependency {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private ApplicationEntry application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depends_on_id")
    private ApplicationEntry dependsOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_type", nullable = false, length = 30)
    private IntegrationType integrationType;

    @Column(columnDefinition = "text")
    private String description;

    public UUID getId() {
        return id;
    }

    public ApplicationEntry getApplication() {
        return application;
    }

    public void setApplication(ApplicationEntry application) {
        this.application = application;
    }

    public ApplicationEntry getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(ApplicationEntry dependsOn) {
        this.dependsOn = dependsOn;
    }

    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    public void setIntegrationType(IntegrationType integrationType) {
        this.integrationType = integrationType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
