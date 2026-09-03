package com.pvg.governance.api;

import com.pvg.governance.domain.AdrSource;
import com.pvg.governance.domain.AdrStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class AdrDtos {

    private AdrDtos() {
    }

    public record AdrCreateRequest(
            @NotBlank @Size(max = 300) String title,
            @NotBlank String context,
            @NotBlank String decision,
            String consequences,
            String alternativesConsidered,
            @NotBlank @Size(max = 200) String author,
            @Size(max = 200) String department,
            String tags,
            boolean aiRelated,
            List<UUID> linkedRiskIds) {
    }

    public record AdrUpdateRequest(
            @NotBlank @Size(max = 300) String title,
            @NotBlank String context,
            @NotBlank String decision,
            String consequences,
            String alternativesConsidered,
            @Size(max = 200) String department,
            String tags,
            boolean aiRelated,
            List<UUID> linkedRiskIds) {
    }

    /** Status transition request; supersededById is required when the new status is SUPERSEDED. */
    public record AdrStatusChangeRequest(
            @NotNull AdrStatus status,
            UUID supersededById) {
    }

    public record LinkedRiskRef(UUID id, String title) {
    }

    public record AdrRef(UUID id, int adrNumber, String title) {
    }

    public record AdrResponse(
            UUID id,
            int adrNumber,
            String title,
            AdrStatus status,
            String context,
            String decision,
            String consequences,
            String alternativesConsidered,
            String author,
            String department,
            String tags,
            boolean aiRelated,
            AdrSource source,
            String sourceRepoUrl,
            String sourcePath,
            AdrRef supersedes,
            AdrRef supersededBy,
            List<LinkedRiskRef> linkedRisks,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
    }
}
