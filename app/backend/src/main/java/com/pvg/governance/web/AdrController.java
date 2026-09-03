package com.pvg.governance.web;

import com.pvg.governance.service.AdrService;
import com.pvg.governance.api.AdrDtos.AdrCreateRequest;
import com.pvg.governance.api.AdrDtos.AdrResponse;
import com.pvg.governance.api.AdrDtos.AdrStatusChangeRequest;
import com.pvg.governance.api.AdrDtos.AdrUpdateRequest;
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

@RestController
@RequestMapping("/api/adrs")
public class AdrController {

    private final AdrService adrService;

    public AdrController(AdrService adrService) {
        this.adrService = adrService;
    }

    /** aiOnly=true is the AI Architecture Register view (proposal §3.6). */
    @GetMapping
    public List<AdrResponse> list(@RequestParam(defaultValue = "false") boolean aiOnly) {
        return adrService.findAll(aiOnly);
    }

    @GetMapping("/search")
    public List<AdrResponse> search(@RequestParam String q) {
        return adrService.search(q);
    }

    @GetMapping("/{id}")
    public AdrResponse get(@PathVariable UUID id) {
        return adrService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdrResponse create(@Valid @RequestBody AdrCreateRequest request,
                              @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return adrService.create(request, ActorHeader.sanitize(actor));
    }

    @PutMapping("/{id}")
    public AdrResponse update(@PathVariable UUID id,
                              @Valid @RequestBody AdrUpdateRequest request,
                              @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return adrService.update(id, request, ActorHeader.sanitize(actor));
    }

    @PostMapping("/{id}/status")
    public AdrResponse changeStatus(@PathVariable UUID id,
                                    @Valid @RequestBody AdrStatusChangeRequest request,
                                    @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return adrService.changeStatus(id, request, ActorHeader.sanitize(actor));
    }
}
