package com.pvg.governance.service;

import com.pvg.governance.domain.AdrStatus;
import com.pvg.governance.repository.AdrRepository;
import com.pvg.governance.repository.ApplicationEntryRepository;
import com.pvg.governance.repository.RiskEntryRepository;
import com.pvg.governance.domain.RiskStatus;
import com.pvg.governance.api.DashboardDtos.DashboardSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ApplicationEntryRepository applicationRepository;
    private final RiskEntryRepository riskRepository;
    private final AdrRepository adrRepository;
    private final RiskService riskService;

    public DashboardService(ApplicationEntryRepository applicationRepository,
                            RiskEntryRepository riskRepository,
                            AdrRepository adrRepository,
                            RiskService riskService) {
        this.applicationRepository = applicationRepository;
        this.riskRepository = riskRepository;
        this.adrRepository = adrRepository;
        this.riskService = riskService;
    }

    public DashboardSummaryResponse summary() {
        var risks = riskRepository.findAll();
        var adrs = adrRepository.findAll();
        Map<String, Long> byCategory = new LinkedHashMap<>();
        risks.stream()
                .filter(risk -> risk.getStatus() != RiskStatus.CLOSED)
                .forEach(risk -> byCategory.merge(risk.getCategory().name(), 1L, Long::sum));
        return new DashboardSummaryResponse(
                applicationRepository.count(),
                risks.stream().filter(risk -> risk.getStatus() != RiskStatus.CLOSED).count(),
                riskService.stale().size(),
                adrs.size(),
                adrs.stream().filter(adr -> adr.getStatus() == AdrStatus.PROPOSED).count(),
                adrs.stream().filter(adr -> adr.isAiRelated()).count(),
                byCategory,
                riskService.topPriority(5));
    }
}
