package com.pvg.governance.web;

import com.pvg.governance.domain.RiskCategory;
import com.pvg.governance.domain.RiskStatus;
import com.pvg.governance.service.RiskService;
import com.pvg.governance.api.RiskDtos.RiskRequest;
import com.pvg.governance.api.RiskDtos.RiskResponse;
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
@RequestMapping("/api/risks")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping
    public List<RiskResponse> list(@RequestParam(required = false) RiskCategory category,
                                   @RequestParam(required = false) RiskStatus status,
                                   @RequestParam(required = false) UUID applicationId) {
        return riskService.findAll(category, status, applicationId);
    }

    @GetMapping("/top-priority")
    public List<RiskResponse> topPriority(@RequestParam(defaultValue = "10") int limit) {
        return riskService.topPriority(Math.clamp(limit, 1, 100));
    }

    @GetMapping("/stale")
    public List<RiskResponse> stale() {
        return riskService.stale();
    }

    @GetMapping("/{id}")
    public RiskResponse get(@PathVariable UUID id) {
        return riskService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RiskResponse create(@Valid @RequestBody RiskRequest request,
                               @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return riskService.create(request, ActorHeader.sanitize(actor));
    }

    @PutMapping("/{id}")
    public RiskResponse update(@PathVariable UUID id,
                               @Valid @RequestBody RiskRequest request,
                               @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return riskService.update(id, request, ActorHeader.sanitize(actor));
    }
}
