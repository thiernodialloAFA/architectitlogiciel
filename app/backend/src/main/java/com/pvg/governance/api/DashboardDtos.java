package com.pvg.governance.api;

import java.util.List;
import java.util.Map;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record DashboardSummaryResponse(
            long applicationCount,
            long openRiskCount,
            long staleRiskCount,
            long adrCount,
            long proposedAdrCount,
            long aiAdrCount,
            Map<String, Long> risksByCategory,
            List<RiskDtos.RiskResponse> topRisks) {
    }
}
