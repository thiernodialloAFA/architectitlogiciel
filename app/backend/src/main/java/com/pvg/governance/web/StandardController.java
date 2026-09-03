package com.pvg.governance.web;

import com.pvg.governance.api.StandardDtos.StandardRequest;
import com.pvg.governance.api.StandardDtos.StandardResponse;
import com.pvg.governance.api.StandardDtos.StandardStatusChangeRequest;
import com.pvg.governance.domain.StandardCategory;
import com.pvg.governance.service.StandardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Standards & reference patterns library (proposal §3.4). */
@RestController
@RequestMapping("/api/standards")
public class StandardController {

    private final StandardService standardService;

    public StandardController(StandardService standardService) {
        this.standardService = standardService;
    }

    @GetMapping
    public List<StandardResponse> list(@RequestParam(required = false) StandardCategory category) {
        return standardService.findAll(category);
    }

    @GetMapping("/{id}")
    public StandardResponse get(@PathVariable UUID id) {
        return standardService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StandardResponse create(@Valid @RequestBody StandardRequest request,
                                   @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return standardService.create(request, ActorHeader.sanitize(actor));
    }

    @PutMapping("/{id}")
    public StandardResponse update(@PathVariable UUID id,
                                   @Valid @RequestBody StandardRequest request,
                                   @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return standardService.update(id, request, ActorHeader.sanitize(actor));
    }

    @PostMapping("/{id}/status")
    public StandardResponse changeStatus(@PathVariable UUID id,
                                         @Valid @RequestBody StandardStatusChangeRequest request,
                                         @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return standardService.changeStatus(id, request, ActorHeader.sanitize(actor));
    }
}
