package org.cauecalil.personalfinance.presentation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cauecalil.personalfinance.application.dto.response.ListTransactionsResponse;
import org.cauecalil.personalfinance.application.usecase.ListTransactionsUseCase;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final ListTransactionsUseCase listTransactionsUseCase;

    @GetMapping
    public ResponseEntity<ListTransactionsResponse> list(
            @RequestParam(required = false) String accountId,
            @RequestParam(defaultValue = "2025-01-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(defaultValue = "2026-01-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "DESC") Sort.Direction sort,
            @RequestHeader(value = "Time-Zone", defaultValue = "America/Sao_Paulo") String timeZone
    ) {
        ZoneId zoneId = resolveZoneId(timeZone);
        Instant fromInstant = fromDate.atStartOfDay(zoneId).toInstant();
        Instant toInstant = toDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant();
        ListTransactionsResponse result = listTransactionsUseCase.execute(accountId, fromInstant, toInstant, page, pageSize, sort);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    private ZoneId resolveZoneId(String timeZone) {
        try {
            return ZoneId.of(timeZone);
        } catch (ZoneRulesException e) {
            log.warn("Invalid Time-Zone header '{}', falling back to America/Sao_Paulo", timeZone);
            return ZoneId.of("America/Sao_Paulo");
        }
    }
}
