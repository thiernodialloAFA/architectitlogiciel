package com.pvg.governance.repository;

import com.pvg.governance.domain.ArbitrationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArbitrationRecordRepository extends JpaRepository<ArbitrationRecord, UUID> {

    List<ArbitrationRecord> findAllByOrderByOccurredAtDesc();

    Optional<ArbitrationRecord> findByProposalId(UUID proposalId);
}
