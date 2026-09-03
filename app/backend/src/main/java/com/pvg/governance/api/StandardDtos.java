package com.pvg.governance.api;

import com.pvg.governance.domain.StandardCategory;
import com.pvg.governance.domain.StandardStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class StandardDtos {

    private StandardDtos() {
    }

    public record StandardRequest(
            @NotBlank @Size(max = 300) String title,
            @NotNull StandardCategory category,
            @NotBlank String content,
            @NotBlank @Size(max = 200) String owner,
            List<UUID> linkedAdrIds,
            List<UUID> appliedApplicationIds) {
    }

    public record StandardStatusChangeRequest(
            @NotNull StandardStatus status) {
    }

    public record StandardResponse(
            UUID id,
            String title,
            StandardCategory category,
            StandardStatus status,
            int version,
            String content,
            String owner,
            List<ForumDtos.AdrRefLite> linkedAdrs,
            List<ForumDtos.ApplicationRefLite> appliedApplications,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
    }
}
