package org.cauecalil.personalfinance.presentation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cauecalil.personalfinance.application.dto.response.TransactionResponse;
import org.cauecalil.personalfinance.application.usecase.ListTransactionsUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final ListTransactionsUseCase listTransactionsUseCase;

    @GetMapping("/{accountId}")
    public ResponseEntity<List<TransactionResponse>> list(
            @PathVariable String accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestHeader(value = "Time-Zone", defaultValue = "America/Sao_Paulo") String timeZone
    ) {
        ZoneId zoneId = resolveZoneId(timeZone);
        Instant fromInstant = from.atStartOfDay(zoneId).toInstant();
        Instant toInstant = to.atTime(LocalTime.MAX).atZone(zoneId).toInstant();
        List<TransactionResponse> result = listTransactionsUseCase.execute(accountId, fromInstant, toInstant);
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
