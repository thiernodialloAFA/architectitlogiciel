package com.pvg.governance.repository;

import com.pvg.governance.domain.ForumSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ForumSessionRepository extends JpaRepository<ForumSession, UUID> {

    List<ForumSession> findAllByOrderBySessionDateDesc();
}
