package com.pvg.governance.repository;

import com.pvg.governance.domain.Adr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdrRepository extends JpaRepository<Adr, UUID> {

    Optional<Adr> findByAdrNumber(int adrNumber);

    Optional<Adr> findBySourceRepoUrlAndSourcePath(String sourceRepoUrl, String sourcePath);

    List<Adr> findByAiRelatedTrueOrderByAdrNumberAsc();

    List<Adr> findAllByOrderByAdrNumberAsc();

    @Query(value = "select nextval('adr_number_seq')", nativeQuery = true)
    int nextAdrNumber();

    /**
     * PostgreSQL full-text search over title/context/decision/consequences/
     * alternatives/tags via the generated tsvector column (proposal §4).
     */
    @Query(value = """
            select * from adr
            where search_vector @@ websearch_to_tsquery('english', :query)
            order by ts_rank(search_vector, websearch_to_tsquery('english', :query)) desc
            """, nativeQuery = true)
    List<Adr> searchFullText(@Param("query") String query);
}
