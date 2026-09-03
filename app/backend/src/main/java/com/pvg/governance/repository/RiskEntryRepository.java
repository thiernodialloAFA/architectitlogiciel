package com.pvg.governance.repository;

import com.pvg.governance.domain.RiskCategory;
import com.pvg.governance.domain.RiskEntry;
import com.pvg.governance.domain.RiskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RiskEntryRepository extends JpaRepository<RiskEntry, UUID> {

    List<RiskEntry> findByApplicationId(UUID applicationId);

    List<RiskEntry> findByCategory(RiskCategory category);

    List<RiskEntry> findByStatus(RiskStatus status);

    /** Entries whose last assessment is older than their review cadence allows. */
    @Query("""
            select r from RiskEntry r
            where r.status not in (com.pvg.governance.domain.RiskStatus.CLOSED)
              and r.lastAssessedAt < :cutoffBase
            """)
    List<RiskEntry> findPotentiallyStale(@Param("cutoffBase") LocalDate cutoffBase);
}
