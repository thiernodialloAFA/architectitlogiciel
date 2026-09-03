package com.pvg.governance.service;

import com.pvg.governance.api.AdrImportDtos.AdrImportDocument;
import com.pvg.governance.api.AdrImportDtos.AdrImportRequest;
import com.pvg.governance.api.AdrImportDtos.AdrImportResponse;
import com.pvg.governance.api.AdrImportDtos.AdrImportResultEntry;
import com.pvg.governance.domain.Adr;
import com.pvg.governance.domain.AdrSource;
import com.pvg.governance.domain.AdrStatus;
import com.pvg.governance.domain.AuditEntityType;
import com.pvg.governance.repository.AdrRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * ADR repository indexing (proposal §3.2, source-of-truth option a): for teams
 * that colocate ADRs as Markdown in their own repositories, the repository stays
 * canonical. Their CI posts the files here (webhook-style batch); the platform
 * keeps a read-only copy purely for cross-department search and linking. The
 * (repoUrl, path) pair is the import identity, so re-imports update the same
 * record — never a duplicate, never a second source of truth. Platform editing
 * of repository-sourced ADRs is rejected in {@link AdrService}.
 */
@Service
@Transactional
public class AdrImportService {

    private final AdrRepository adrRepository;
    private final AuditService auditService;

    public AdrImportService(AdrRepository adrRepository, AuditService auditService) {
        this.adrRepository = adrRepository;
        this.auditService = auditService;
    }

    public AdrImportResponse importBatch(AdrImportRequest request, String actor) {
        List<AdrImportResultEntry> results = new ArrayList<>();
        int created = 0;
        int updated = 0;
        for (AdrImportDocument document : request.documents()) {
            ParsedAdr parsed = parse(document);
            Adr existing = adrRepository
                    .findBySourceRepoUrlAndSourcePath(request.repoUrl(), document.path())
                    .orElse(null);
            if (existing == null) {
                Adr adr = new Adr();
                adr.setAdrNumber(adrRepository.nextAdrNumber());
                adr.setSource(AdrSource.REPOSITORY);
                adr.setSourceRepoUrl(request.repoUrl());
                adr.setSourcePath(document.path());
                applyParsed(adr, parsed);
                Adr saved = adrRepository.save(adr);
                auditService.recordCreated(AuditEntityType.ADR, saved.getId(),
                        "Indexed from " + request.repoUrl() + "/" + document.path()
                                + ": ADR-" + saved.getAdrNumber() + " " + saved.getTitle(),
                        actor);
                created++;
                results.add(new AdrImportResultEntry(document.path(), saved.getId(),
                        saved.getAdrNumber(), saved.getTitle(), "created"));
            } else if (differs(existing, parsed)) {
                recordChanges(existing, parsed, actor);
                applyParsed(existing, parsed);
                Adr saved = adrRepository.save(existing);
                updated++;
                results.add(new AdrImportResultEntry(document.path(), saved.getId(),
                        saved.getAdrNumber(), saved.getTitle(), "updated"));
            } else {
                results.add(new AdrImportResultEntry(document.path(), existing.getId(),
                        existing.getAdrNumber(), existing.getTitle(), "unchanged"));
            }
        }
        return new AdrImportResponse(request.repoUrl(), created, updated, results);
    }

    private void applyParsed(Adr adr, ParsedAdr parsed) {
        adr.setTitle(parsed.title());
        adr.setStatus(parsed.status());
        adr.setContext(parsed.context());
        adr.setDecision(parsed.decision());
        adr.setConsequences(parsed.consequences());
        adr.setAlternativesConsidered(parsed.alternatives());
        adr.setAuthor(parsed.author());
        adr.setTags(parsed.tags());
        adr.setAiRelated(parsed.aiRelated());
    }

    private boolean differs(Adr adr, ParsedAdr parsed) {
        return !Objects.equals(adr.getTitle(), parsed.title())
                || adr.getStatus() != parsed.status()
                || !Objects.equals(adr.getContext(), parsed.context())
                || !Objects.equals(adr.getDecision(), parsed.decision())
                || !Objects.equals(adr.getConsequences(), parsed.consequences())
                || !Objects.equals(adr.getAlternativesConsidered(), parsed.alternatives())
                || !Objects.equals(adr.getAuthor(), parsed.author())
                || !Objects.equals(adr.getTags(), parsed.tags())
                || adr.isAiRelated() != parsed.aiRelated();
    }

    private void recordChanges(Adr adr, ParsedAdr parsed, String actor) {
        UUID id = adr.getId();
        auditService.recordFieldChange(AuditEntityType.ADR, id, "title", adr.getTitle(), parsed.title(), actor);
        auditService.recordFieldChange(AuditEntityType.ADR, id, "context", adr.getContext(), parsed.context(), actor);
        auditService.recordFieldChange(AuditEntityType.ADR, id, "decision", adr.getDecision(), parsed.decision(), actor);
        auditService.recordFieldChange(AuditEntityType.ADR, id, "consequences", adr.getConsequences(), parsed.consequences(), actor);
        auditService.recordFieldChange(AuditEntityType.ADR, id, "alternativesConsidered",
                adr.getAlternativesConsidered(), parsed.alternatives(), actor);
        auditService.recordFieldChange(AuditEntityType.ADR, id, "author", adr.getAuthor(), parsed.author(), actor);
        auditService.recordFieldChange(AuditEntityType.ADR, id, "tags", adr.getTags(), parsed.tags(), actor);
        if (adr.getStatus() != parsed.status()) {
            auditService.recordStatusChange(AuditEntityType.ADR, id, adr.getStatus(), parsed.status(), actor);
        }
    }

    /**
     * Parses a Nygard/MADR-style Markdown ADR: the first {@code #} heading is the
     * title; {@code ##} headings delimit the Status / Context / Decision /
     * Consequences / Alternatives Considered / Deciders / Tags sections.
     */
    private ParsedAdr parse(AdrImportDocument document) {
        String title = null;
        Map<String, StringBuilder> sections = new LinkedHashMap<>();
        StringBuilder current = null;
        for (String line : document.markdown().split("\r?\n", -1)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("# ") && !trimmed.startsWith("## ")) {
                if (title == null) {
                    title = trimmed.substring(2).trim();
                }
                current = null;
            } else if (trimmed.startsWith("## ")) {
                String name = normalizeSectionName(trimmed.substring(3));
                current = sections.computeIfAbsent(name, key -> new StringBuilder());
            } else if (current != null) {
                if (!current.isEmpty()) {
                    current.append('\n');
                }
                current.append(line);
            }
        }
        if (title == null || title.isBlank()) {
            throw new IllegalStateException("ADR file '" + document.path()
                    + "' has no top-level '# Title' heading and cannot be indexed");
        }
        String context = sectionText(sections, "context");
        String decision = sectionText(sections, "decision");
        if (context == null || decision == null) {
            throw new IllegalStateException("ADR file '" + document.path()
                    + "' is missing the required '## Context' or '## Decision' section");
        }
        String author = sectionText(sections, "author");
        if (author == null) {
            author = sectionText(sections, "deciders");
        }
        String tags = sectionText(sections, "tags");
        return new ParsedAdr(
                truncate(title, 300),
                parseStatus(sectionText(sections, "status")),
                context,
                decision,
                sectionText(sections, "consequences"),
                sectionText(sections, "alternatives"),
                author == null ? "Repository import" : truncate(author, 200),
                tags,
                hasAiTag(tags));
    }

    private static String normalizeSectionName(String rawName) {
        String name = rawName.trim().toLowerCase(Locale.ROOT);
        if (name.startsWith("alternatives") || name.startsWith("considered")) {
            return "alternatives";
        }
        return name;
    }

    private static String sectionText(Map<String, StringBuilder> sections, String name) {
        StringBuilder builder = sections.get(name);
        if (builder == null) {
            return null;
        }
        String text = builder.toString().strip();
        return text.isEmpty() ? null : text;
    }

    private static AdrStatus parseStatus(String statusSection) {
        if (statusSection == null) {
            return AdrStatus.PROPOSED;
        }
        String firstWord = statusSection.strip().split("\\s+", 2)[0]
                .replaceAll("[^A-Za-z]", "")
                .toUpperCase(Locale.ROOT);
        return switch (firstWord) {
            case "ACCEPTED" -> AdrStatus.ACCEPTED;
            case "DEPRECATED" -> AdrStatus.DEPRECATED;
            case "SUPERSEDED" -> AdrStatus.SUPERSEDED;
            default -> AdrStatus.PROPOSED;
        };
    }

    private static boolean hasAiTag(String tags) {
        return tags != null && Arrays.stream(tags.split(","))
                .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                .anyMatch(tag -> tag.equals("ai") || tag.equals("llm"));
    }

    private static String truncate(String value, int max) {
        return value.length() > max ? value.substring(0, max) : value;
    }

    private record ParsedAdr(String title, AdrStatus status, String context, String decision,
                             String consequences, String alternatives, String author,
                             String tags, boolean aiRelated) {
    }
}
