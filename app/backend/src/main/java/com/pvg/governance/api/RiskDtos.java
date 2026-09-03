package com.pvg.governance.api;

import com.pvg.governance.domain.RatingLevel;
import com.pvg.governance.domain.RiskCategory;
import com.pvg.governance.domain.RiskStatus;
import com.pvg.governance.domain.TreatmentDecision;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class RiskDtos {

    private RiskDtos() {
    }

    public record RiskRequest(
            @NotNull UUID applicationId,
            @NotBlank @Size(max = 300) String title,
            @NotNull RiskCategory category,
            @NotBlank String description,
            String currentControls,
            @NotNull RatingLevel inherentImpact,
            @NotNull RatingLevel inherentLikelihood,
            @NotNull RatingLevel residualImpact,
            @NotNull RatingLevel residualLikelihood,
            String blastRadius,
            @NotNull TreatmentDecision treatmentDecision,
            @Size(max = 100) String costToFix,
            LocalDate targetDate,
            @NotNull RiskStatus status,
            @NotBlank @Size(max = 200) String riskOwner,
            String evidenceLink,
            @Min(1) @Max(3650) int reviewCadenceDays,
            @NotNull LocalDate lastAssessedAt) {
    }

    public record RiskResponse(
            UUID id,
            UUID applicationId,
            String applicationName,
            String title,
            RiskCategory category,
            String description,
            String currentControls,
            RatingLevel inherentImpact,
            RatingLevel inherentLikelihood,
            RatingLevel residualImpact,
            RatingLevel residualLikelihood,
            int residualScore,
            String blastRadius,
            TreatmentDecision treatmentDecision,
            String costToFix,
            LocalDate targetDate,
            RiskStatus status,
            String riskOwner,
            String evidenceLink,
            int reviewCadenceDays,
            LocalDate lastAssessedAt,
            boolean stale,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
    }
}
