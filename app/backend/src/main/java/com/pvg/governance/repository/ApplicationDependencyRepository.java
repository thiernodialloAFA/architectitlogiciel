package com.pvg.governance.repository;

import com.pvg.governance.domain.ApplicationDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationDependencyRepository extends JpaRepository<ApplicationDependency, UUID> {

    List<ApplicationDependency> findByApplicationId(UUID applicationId);

    List<ApplicationDependency> findByDependsOnId(UUID dependsOnId);
}
