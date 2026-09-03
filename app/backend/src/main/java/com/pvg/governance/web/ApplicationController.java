package com.pvg.governance.web;

import com.pvg.governance.service.ApplicationService;
import com.pvg.governance.api.ApplicationDtos.ApplicationRequest;
import com.pvg.governance.api.ApplicationDtos.ApplicationResponse;
import com.pvg.governance.api.ApplicationDtos.DependencyRequest;
import com.pvg.governance.api.ApplicationDtos.DependencyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<ApplicationResponse> list() {
        return applicationService.findAll();
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable UUID id) {
        return applicationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@Valid @RequestBody ApplicationRequest request,
                                      @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return applicationService.create(request, ActorHeader.sanitize(actor));
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable UUID id,
                                      @Valid @RequestBody ApplicationRequest request,
                                      @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return applicationService.update(id, request, ActorHeader.sanitize(actor));
    }

    @GetMapping("/{id}/dependencies")
    public List<DependencyResponse> dependencies(@PathVariable UUID id) {
        return applicationService.dependenciesOf(id);
    }

    @GetMapping("/{id}/dependents")
    public List<DependencyResponse> dependents(@PathVariable UUID id) {
        return applicationService.dependentsOn(id);
    }

    @PostMapping("/{id}/dependencies")
    @ResponseStatus(HttpStatus.CREATED)
    public DependencyResponse addDependency(@PathVariable UUID id,
                                            @Valid @RequestBody DependencyRequest request,
                                            @RequestHeader(value = ActorHeader.NAME, required = false) String actor) {
        return applicationService.addDependency(id, request, ActorHeader.sanitize(actor));
    }
}
