package com.cauecalil.personalfinance.presentation.controller;

import com.cauecalil.personalfinance.application.dto.response.GetDashboardCashflowResponse;
import com.cauecalil.personalfinance.application.dto.response.GetDashboardCategoriesResponse;
import com.cauecalil.personalfinance.application.dto.response.GetDashboardMetricsResponse;
import com.cauecalil.personalfinance.application.usecase.GetDashboardCashflowUseCase;
import com.cauecalil.personalfinance.application.usecase.GetDashboardCategoriesUseCase;
import com.cauecalil.personalfinance.application.usecase.GetDashboardMetricsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    private final GetDashboardMetricsUseCase getDashboardMetricsUseCase;
    private final GetDashboardCategoriesUseCase getDashboardCategoriesUseCase;
    private final GetDashboardCashflowUseCase getDashboardCashflowUseCase;

    @GetMapping("/metrics")
    public ResponseEntity<GetDashboardMetricsResponse> metrics(
            @RequestParam(required = false) String accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestHeader(value = "Time-Zone", defaultValue = "America/Sao_Paulo") String timeZone
    ) {
        ZoneId zoneId = resolveZoneId(timeZone);
        Instant fromInstant = fromDate.atStartOfDay(zoneId).toInstant();
        Instant toInstant = toDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant();
        GetDashboardMetricsResponse result = getDashboardMetricsUseCase.execute(accountId, fromInstant, toInstant);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/categories")
    public ResponseEntity<GetDashboardCategoriesResponse> categories(
            @RequestParam(required = false) String accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestHeader(value = "Time-Zone", defaultValue = "America/Sao_Paulo") String timeZone
    ) {
        ZoneId zoneId = resolveZoneId(timeZone);
        Instant fromInstant = fromDate.atStartOfDay(zoneId).toInstant();
        Instant toInstant = toDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant();
        GetDashboardCategoriesResponse result = getDashboardCategoriesUseCase.execute(accountId, fromInstant, toInstant);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/cashflow")
    public ResponseEntity<GetDashboardCashflowResponse> cashflow(
            @RequestParam(required = false) String accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestHeader(value = "Time-Zone", defaultValue = "America/Sao_Paulo") String timeZone
    ) {
        ZoneId zoneId = resolveZoneId(timeZone);
        Instant fromInstant = fromDate.atStartOfDay(zoneId).toInstant();
        Instant toInstant = toDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant();
        GetDashboardCashflowResponse result = getDashboardCashflowUseCase.execute(accountId, fromInstant, toInstant, zoneId);
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
