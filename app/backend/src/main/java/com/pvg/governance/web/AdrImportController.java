package com.pvg.governance.web;

import com.pvg.governance.api.AdrImportDtos.AdrImportRequest;
import com.pvg.governance.api.AdrImportDtos.AdrImportResponse;
import com.pvg.governance.service.AdrImportService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ADR repository indexing (proposal §3.2 option a): a webhook-style batch import
 * of Markdown ADR files for teams whose repository is the canonical store. The
 * import is idempotent on (repoUrl, path); indexed records are read-only in the
 * platform.
 */
@RestController
@RequestMapping("/api/adr-imports")
public class AdrImportController {

    private final AdrImportService adrImportService;

    public AdrImportController(AdrImportService adrImportService) {
        this.adrImportService = adrImportService;
    }

    @PostMapping
    public AdrImportResponse importBatch(@Valid @RequestBody AdrImportRequest request,
                                         @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return adrImportService.importBatch(request, ActorHeader.sanitize(actor));
    }
}
