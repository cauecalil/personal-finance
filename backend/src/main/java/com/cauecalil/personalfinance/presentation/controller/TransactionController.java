package com.cauecalil.personalfinance.presentation.controller;

import com.cauecalil.personalfinance.application.dto.response.ListTransactionsResponse;
import com.cauecalil.personalfinance.application.usecase.ListTransactionsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
@Tag(name = "Transactions", description = "Operations for paginated transaction retrieval with filtering and sorting.")
public class TransactionController {
    private final ListTransactionsUseCase listTransactionsUseCase;

    @GetMapping
    @Operation(
        summary = "List transactions",
        description = "Return paginated transactions filtered by optional account and date range, with configurable page size and sort direction."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Transactions were retrieved successfully.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ListTransactionsResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request validation failed or date range is invalid.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Requested account was not found.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "An unexpected server error occurred.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    public ResponseEntity<ListTransactionsResponse> list(
        @Parameter(description = "Optional account identifier to filter transactions by account.", example = "f4e3a2b1-6d5c-4b3a-9f87-1d2c3b4a5e6f")
            @RequestParam(required = false) String accountId,
        @Parameter(description = "Start date in ISO 8601 date format (yyyy-MM-dd).", example = "2026-01-01")
            @RequestParam(defaultValue = "2025-01-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @Parameter(description = "End date in ISO 8601 date format (yyyy-MM-dd).", example = "2026-12-31")
            @RequestParam(defaultValue = "2026-01-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @Parameter(description = "Zero-based page index.", example = "0")
            @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Maximum number of items per page.", example = "20")
            @RequestParam(defaultValue = "20") int pageSize,
        @Parameter(description = "Sort direction by transaction date. Possible values: ASC, DESC.", example = "DESC")
            @RequestParam(defaultValue = "DESC") Sort.Direction sort,
        @Parameter(description = "IANA time zone used to interpret date boundaries.", example = "America/Sao_Paulo")
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
