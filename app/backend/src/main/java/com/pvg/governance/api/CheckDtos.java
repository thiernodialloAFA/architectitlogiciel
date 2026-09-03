package com.pvg.governance.api;

import com.pvg.governance.domain.CheckStatus;
import com.pvg.governance.domain.CheckType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class CheckDtos {

    private CheckDtos() {
    }

    public record CheckCreateRequest(
            @NotBlank @Size(max = 300) String name,
            @NotNull CheckType checkType,
            @NotBlank @Size(max = 100) String tool,
            String link,
            String description) {
    }

    /** Status push from CI (webhook) or a manual update; runAt defaults to now. */
    public record CheckStatusUpdateRequest(
            @NotNull CheckStatus status,
            OffsetDateTime runAt) {
    }

    public record CheckResponse(
            UUID id,
            UUID applicationId,
            String applicationName,
            String name,
            CheckType checkType,
            String tool,
            String link,
            String description,
            CheckStatus lastStatus,
            OffsetDateTime lastRunAt,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
    }
}
