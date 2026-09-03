package com.pvg.governance.repository;

import com.pvg.governance.domain.C4Diagram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface C4DiagramRepository extends JpaRepository<C4Diagram, UUID> {

    List<C4Diagram> findByApplicationIdOrderByVersionDesc(UUID applicationId);

    Optional<C4Diagram> findFirstByApplicationIdOrderByVersionDesc(UUID applicationId);
}
