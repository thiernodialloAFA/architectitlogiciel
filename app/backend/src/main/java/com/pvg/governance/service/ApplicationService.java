package com.pvg.governance.service;

import com.pvg.governance.domain.ApplicationDependency;
import com.pvg.governance.domain.ApplicationEntry;
import com.pvg.governance.domain.AuditEntityType;
import com.pvg.governance.domain.RiskStatus;
import com.pvg.governance.repository.ApplicationDependencyRepository;
import com.pvg.governance.repository.ApplicationEntryRepository;
import com.pvg.governance.repository.RiskEntryRepository;
import com.pvg.governance.api.ApplicationDtos.ApplicationRequest;
import com.pvg.governance.api.ApplicationDtos.ApplicationResponse;
import com.pvg.governance.api.ApplicationDtos.DependencyRequest;
import com.pvg.governance.api.ApplicationDtos.DependencyResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationEntryRepository applicationRepository;
    private final ApplicationDependencyRepository dependencyRepository;
    private final RiskEntryRepository riskRepository;
    private final AuditService auditService;

    public ApplicationService(ApplicationEntryRepository applicationRepository,
                              ApplicationDependencyRepository dependencyRepository,
                              RiskEntryRepository riskRepository,
                              AuditService auditService) {
        this.applicationRepository = applicationRepository;
        this.dependencyRepository = dependencyRepository;
        this.riskRepository = riskRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll() {
        return applicationRepository.findAll().stream()
                .sorted(Comparator.comparing(ApplicationEntry::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public ApplicationResponse create(ApplicationRequest request, String actor) {
        applicationRepository.findByNameIgnoreCase(request.name()).ifPresent(existing -> {
            throw new IllegalStateException("An application named '" + request.name() + "' already exists");
        });
        ApplicationEntry entity = new ApplicationEntry();
        apply(entity, request);
        ApplicationEntry saved = applicationRepository.save(entity);
        auditService.recordCreated(AuditEntityType.APPLICATION, saved.getId(), saved.getName(), actor);
        return toResponse(saved);
    }

    public ApplicationResponse update(UUID id, ApplicationRequest request, String actor) {
        ApplicationEntry entity = getOrThrow(id);
        auditService.recordFieldChange(AuditEntityType.APPLICATION, id, "name", entity.getName(), request.name(), actor);
        auditService.recordFieldChange(AuditEntityType.APPLICATION, id, "description", entity.getDescription(), request.description(), actor);
        auditService.recordFieldChange(AuditEntityType.APPLICATION, id, "ownerTeam", entity.getOwnerTeam(), request.ownerTeam(), actor);
        auditService.recordFieldChange(AuditEntityType.APPLICATION, id, "ownerDepartment", entity.getOwnerDepartment(), request.ownerDepartment(), actor);
        auditService.recordFieldChange(AuditEntityType.APPLICATION, id, "lifecycleStatus", entity.getLifecycleStatus(), request.lifecycleStatus(), actor);
        auditService.recordFieldChange(AuditEntityType.APPLICATION, id, "c4ModelLink", entity.getC4ModelLink(), request.c4ModelLink(), actor);
        auditService.recordFieldChange(AuditEntityType.APPLICATION, id, "adrLogLink", entity.getAdrLogLink(), request.adrLogLink(), actor);
        apply(entity, request);
        return toResponse(applicationRepository.save(entity));
    }

    public DependencyResponse addDependency(UUID applicationId, DependencyRequest request, String actor) {
        ApplicationEntry application = getOrThrow(applicationId);
        ApplicationEntry dependsOn = getOrThrow(request.dependsOnId());
        if (application.getId().equals(dependsOn.getId())) {
            throw new IllegalStateException("An application cannot depend on itself");
        }
        ApplicationDependency dependency = new ApplicationDependency();
        dependency.setApplication(application);
        dependency.setDependsOn(dependsOn);
        dependency.setIntegrationType(request.integrationType());
        dependency.setDescription(request.description());
        ApplicationDependency saved = dependencyRepository.save(dependency);
        auditService.recordFieldChange(AuditEntityType.APPLICATION, applicationId, "dependencies",
                null, "depends on " + dependsOn.getName() + " (" + request.integrationType() + ")", actor);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DependencyResponse> dependenciesOf(UUID applicationId) {
        getOrThrow(applicationId);
        return dependencyRepository.findByApplicationId(applicationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DependencyResponse> dependentsOn(UUID applicationId) {
        getOrThrow(applicationId);
        return dependencyRepository.findByDependsOnId(applicationId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ApplicationEntry getOrThrow(UUID id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application " + id + " not found"));
    }

    private void apply(ApplicationEntry entity, ApplicationRequest request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setOwnerTeam(request.ownerTeam());
        entity.setOwnerDepartment(request.ownerDepartment());
        entity.setLifecycleStatus(request.lifecycleStatus());
        entity.setC4ModelLink(request.c4ModelLink());
        entity.setAdrLogLink(request.adrLogLink());
    }

    private ApplicationResponse toResponse(ApplicationEntry entity) {
        List<DependencyResponse> dependencies = dependencyRepository.findByApplicationId(entity.getId()).stream()
                .map(this::toResponse)
                .toList();
        int openRisks = (int) riskRepository.findByApplicationId(entity.getId()).stream()
                .filter(risk -> risk.getStatus() != RiskStatus.CLOSED)
                .count();
        return new ApplicationResponse(entity.getId(), entity.getName(), entity.getDescription(),
                entity.getOwnerTeam(), entity.getOwnerDepartment(), entity.getLifecycleStatus(),
                entity.getC4ModelLink(), entity.getAdrLogLink(), dependencies, openRisks,
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private DependencyResponse toResponse(ApplicationDependency dependency) {
        return new DependencyResponse(dependency.getId(),
                dependency.getApplication().getId(), dependency.getApplication().getName(),
                dependency.getDependsOn().getId(), dependency.getDependsOn().getName(),
                dependency.getIntegrationType(), dependency.getDescription());
    }
}
