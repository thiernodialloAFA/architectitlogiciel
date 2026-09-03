package com.pvg.governance.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** One entry in the application landscape inventory (proposal §3.1). */
@Entity
@Table(name = "application_entry")
public class ApplicationEntry {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "owner_team", nullable = false, length = 200)
    private String ownerTeam;

    @Column(name = "owner_department", nullable = false, length = 200)
    private String ownerDepartment;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_status", nullable = false, length = 30)
    private LifecycleStatus lifecycleStatus;

    @Column(name = "c4_model_link", columnDefinition = "text")
    private String c4ModelLink;

    @Column(name = "adr_log_link", columnDefinition = "text")
    private String adrLogLink;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationDependency> dependencies = new ArrayList<>();

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerTeam() {
        return ownerTeam;
    }

    public void setOwnerTeam(String ownerTeam) {
        this.ownerTeam = ownerTeam;
    }

    public String getOwnerDepartment() {
        return ownerDepartment;
    }

    public void setOwnerDepartment(String ownerDepartment) {
        this.ownerDepartment = ownerDepartment;
    }

    public LifecycleStatus getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(LifecycleStatus lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    public String getC4ModelLink() {
        return c4ModelLink;
    }

    public void setC4ModelLink(String c4ModelLink) {
        this.c4ModelLink = c4ModelLink;
    }

    public String getAdrLogLink() {
        return adrLogLink;
    }

    public void setAdrLogLink(String adrLogLink) {
        this.adrLogLink = adrLogLink;
    }

    public List<ApplicationDependency> getDependencies() {
        return dependencies;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
