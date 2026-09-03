package com.pvg.governance.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class C4Dtos {

    private C4Dtos() {
    }

    public record C4DiagramCreateRequest(
            @NotBlank @Size(max = 200) String label,
            @NotBlank String source) {
    }

    public record C4DiagramResponse(
            UUID id,
            UUID applicationId,
            int version,
            String label,
            String source,
            String author,
            OffsetDateTime createdAt) {
    }
}
