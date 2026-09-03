package com.pvg.governance.web;

import com.pvg.governance.api.C4Dtos.C4DiagramCreateRequest;
import com.pvg.governance.api.C4Dtos.C4DiagramResponse;
import com.pvg.governance.service.C4DiagramService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Versioned textual C4 per landscape entry (proposal §3.5). Versions are
 * append-only: POST adds the next version; there is no update or delete.
 */
@RestController
@RequestMapping("/api/applications/{applicationId}/c4")
public class C4DiagramController {

    private final C4DiagramService c4DiagramService;

    public C4DiagramController(C4DiagramService c4DiagramService) {
        this.c4DiagramService = c4DiagramService;
    }

    @GetMapping
    public List<C4DiagramResponse> versions(@PathVariable UUID applicationId) {
        return c4DiagramService.versionsFor(applicationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public C4DiagramResponse addVersion(@PathVariable UUID applicationId,
                                        @Valid @RequestBody C4DiagramCreateRequest request,
                                        @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return c4DiagramService.addVersion(applicationId, request, ActorHeader.sanitize(actor));
    }
}
