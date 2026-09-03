package com.pvg.governance.repository;

import com.pvg.governance.domain.CheckReference;
import com.pvg.governance.domain.CheckStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CheckReferenceRepository extends JpaRepository<CheckReference, UUID> {

    List<CheckReference> findByApplicationIdOrderByNameAsc(UUID applicationId);

    long countByLastStatus(CheckStatus status);
}
