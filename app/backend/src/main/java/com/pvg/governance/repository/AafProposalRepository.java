package com.pvg.governance.repository;

import com.pvg.governance.domain.AafProposal;
import com.pvg.governance.domain.ProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AafProposalRepository extends JpaRepository<AafProposal, UUID> {

    List<AafProposal> findAllByOrderByCreatedAtDesc();

    List<AafProposal> findByStatusOrderByCreatedAtDesc(ProposalStatus status);

    List<AafProposal> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    long countByStatusNot(ProposalStatus status);
}
