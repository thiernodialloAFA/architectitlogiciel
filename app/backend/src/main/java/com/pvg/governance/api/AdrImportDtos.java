package com.pvg.governance.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public final class AdrImportDtos {

    private AdrImportDtos() {
    }

    /**
     * A webhook-style batch import (§3.2 option a): the owning team's CI posts the
     * Markdown ADR files of its repository; re-posting the same (repoUrl, path)
     * updates the existing read-only record instead of duplicating it.
     */
    public record AdrImportRequest(
            @NotBlank @Size(max = 500) String repoUrl,
            @NotEmpty List<@Valid AdrImportDocument> documents) {
    }

    public record AdrImportDocument(
            @NotBlank @Size(max = 500) String path,
            @NotBlank String markdown) {
    }

    public record AdrImportResultEntry(
            String path,
            UUID adrId,
            int adrNumber,
            String title,
            String outcome) {
    }

    public record AdrImportResponse(
            String repoUrl,
            int created,
            int updated,
            List<AdrImportResultEntry> results) {
    }
}
