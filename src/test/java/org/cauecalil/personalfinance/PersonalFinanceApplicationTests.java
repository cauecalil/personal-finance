package org.cauecalil.personalfinance;

import org.cauecalil.personalfinance.domain.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.ZoneId;

@SpringBootTest
class PersonalFinanceApplicationTests {

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldExecuteCashflowAggregationQueryForAllGranularities() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-12-31T23:59:59Z");
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");

        for (TransactionRepository.CashflowGranularity granularity : TransactionRepository.CashflowGranularity.values()) {
            transactionRepository.findCashflowAggregations(null, from, to, granularity, zoneId);
        }
    }

}
