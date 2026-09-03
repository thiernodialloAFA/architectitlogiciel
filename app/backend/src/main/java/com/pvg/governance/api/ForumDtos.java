package com.pvg.governance.api;

import com.pvg.governance.domain.AafScope;
import com.pvg.governance.domain.ForumSessionStatus;
import com.pvg.governance.domain.ProposalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ForumDtos {

    private ForumDtos() {
    }

    public record SessionCreateRequest(
            @NotNull LocalDate sessionDate,
            @NotBlank @Size(max = 300) String title,
            String notes) {
    }

    public record SessionHoldRequest(
            String notes) {
    }

    public record SessionResponse(
            UUID id,
            LocalDate sessionDate,
            String title,
            ForumSessionStatus status,
            String notes,
            int agendaSize,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
    }

    public record ProposalCreateRequest(
            @NotBlank @Size(max = 300) String title,
            @NotBlank String summary,
            @NotNull AafScope scope,
            @NotBlank @Size(max = 200) String submittedBy,
            @Size(max = 200) String department,
            UUID adrId,
            UUID applicationId) {
    }

    public record ProposalScheduleRequest(
            @NotNull UUID sessionId) {
    }

    /** Outcome capture (proposal §3.3): advice given, by whom, and the proposer's final decision. */
    public record ProposalOutcomeRequest(
            @NotBlank String adviceGiven,
            @NotBlank @Size(max = 300) String advisedBy,
            @NotBlank String finalDecision,
            UUID resultingAdrId) {
    }

    public record ArbitrationRequest(
            @NotBlank @Size(max = 200) String requestedBy,
            @NotBlank String rationale,
            @NotBlank String outcome,
            @NotBlank @Size(max = 200) String decidedBy) {
    }

    public record AdrRefLite(UUID id, int adrNumber, String title) {
    }

    public record ApplicationRefLite(UUID id, String name) {
    }

    public record SessionRefLite(UUID id, LocalDate sessionDate, String title) {
    }

    public record ProposalResponse(
            UUID id,
            String title,
            String summary,
            AafScope scope,
            ProposalStatus status,
            String submittedBy,
            String department,
            AdrRefLite adr,
            ApplicationRefLite application,
            SessionRefLite session,
            String adviceGiven,
            String advisedBy,
            String finalDecision,
            OffsetDateTime decidedAt,
            boolean arbitrated,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
    }

    public record ArbitrationResponse(
            UUID id,
            UUID proposalId,
            String proposalTitle,
            String requestedBy,
            String rationale,
            String outcome,
            String decidedBy,
            OffsetDateTime occurredAt) {
    }

    public record SessionDetailResponse(
            SessionResponse session,
            List<ProposalResponse> agenda) {
    }
}
