package com.pvg.governance.repository;

import com.pvg.governance.domain.Standard;
import com.pvg.governance.domain.StandardCategory;
import com.pvg.governance.domain.StandardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StandardRepository extends JpaRepository<Standard, UUID> {

    List<Standard> findAllByOrderByTitleAsc();

    List<Standard> findByCategoryOrderByTitleAsc(StandardCategory category);

    long countByStatus(StandardStatus status);
}
