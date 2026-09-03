package com.pvg.governance.repository;

import com.pvg.governance.domain.ApplicationEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationEntryRepository extends JpaRepository<ApplicationEntry, UUID> {

    Optional<ApplicationEntry> findByNameIgnoreCase(String name);
}
