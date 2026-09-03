package com.pvg.governance.web;

import com.pvg.governance.api.CheckDtos.CheckCreateRequest;
import com.pvg.governance.api.CheckDtos.CheckResponse;
import com.pvg.governance.api.CheckDtos.CheckStatusUpdateRequest;
import com.pvg.governance.service.CheckReferenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Fitness-function / contract-test check references (proposal §6 item 6). CI
 * pushes pass/fail via POST /api/checks/{id}/status (webhook-style); the same
 * endpoint serves the manual first pass. Status history lives in the audit trail.
 */
@RestController
public class CheckController {

    private final CheckReferenceService checkService;

    public CheckController(CheckReferenceService checkService) {
        this.checkService = checkService;
    }

    @GetMapping("/api/applications/{applicationId}/checks")
    public List<CheckResponse> checksFor(@PathVariable UUID applicationId) {
        return checkService.checksFor(applicationId);
    }

    @PostMapping("/api/applications/{applicationId}/checks")
    @ResponseStatus(HttpStatus.CREATED)
    public CheckResponse create(@PathVariable UUID applicationId,
                                @Valid @RequestBody CheckCreateRequest request,
                                @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return checkService.create(applicationId, request, ActorHeader.sanitize(actor));
    }

    @PostMapping("/api/checks/{id}/status")
    public CheckResponse updateStatus(@PathVariable UUID id,
                                      @Valid @RequestBody CheckStatusUpdateRequest request,
                                      @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return checkService.updateStatus(id, request, ActorHeader.sanitize(actor));
    }
}
