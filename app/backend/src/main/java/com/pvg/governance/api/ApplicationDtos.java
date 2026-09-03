package com.pvg.governance.api;

import com.pvg.governance.domain.IntegrationType;
import com.pvg.governance.domain.LifecycleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ApplicationDtos {

    private ApplicationDtos() {
    }

    public record ApplicationRequest(
            @NotBlank @Size(max = 200) String name,
            String description,
            @NotBlank @Size(max = 200) String ownerTeam,
            @NotBlank @Size(max = 200) String ownerDepartment,
            @NotNull LifecycleStatus lifecycleStatus,
            String c4ModelLink,
            String adrLogLink) {
    }

    public record DependencyRequest(
            @NotNull UUID dependsOnId,
            @NotNull IntegrationType integrationType,
            String description) {
    }

    public record DependencyResponse(
            UUID id,
            UUID applicationId,
            String applicationName,
            UUID dependsOnId,
            String dependsOnName,
            IntegrationType integrationType,
            String description) {
    }

    public record ApplicationResponse(
            UUID id,
            String name,
            String description,
            String ownerTeam,
            String ownerDepartment,
            LifecycleStatus lifecycleStatus,
            String c4ModelLink,
            String adrLogLink,
            List<DependencyResponse> dependencies,
            int openRiskCount,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
    }
}
