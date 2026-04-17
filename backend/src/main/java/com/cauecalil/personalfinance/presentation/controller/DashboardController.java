package com.cauecalil.personalfinance.presentation.controller;

import com.cauecalil.personalfinance.application.dto.response.GetDashboardCashflowResponse;
import com.cauecalil.personalfinance.application.dto.response.GetDashboardCategoriesResponse;
import com.cauecalil.personalfinance.application.dto.response.GetDashboardMetricsResponse;
import com.cauecalil.personalfinance.application.usecase.GetDashboardCashflowUseCase;
import com.cauecalil.personalfinance.application.usecase.GetDashboardCategoriesUseCase;
import com.cauecalil.personalfinance.application.usecase.GetDashboardMetricsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Operations for retrieving portfolio metrics, categories, and cashflow analytics.")
public class DashboardController {
    private final GetDashboardMetricsUseCase getDashboardMetricsUseCase;
    private final GetDashboardCategoriesUseCase getDashboardCategoriesUseCase;
    private final GetDashboardCashflowUseCase getDashboardCashflowUseCase;

    @GetMapping("/metrics")
        @Operation(
            summary = "Get dashboard metrics",
            description = "Return high-level balance and period totals for income and expenses. Date boundaries are interpreted using the provided time zone header."
        )
        @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "Dashboard metrics were retrieved successfully.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetDashboardMetricsResponse.class))
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
    public ResponseEntity<GetDashboardMetricsResponse> metrics(
            @Parameter(description = "Optional account identifier to scope metrics to a single account.", example = "f4e3a2b1-6d5c-4b3a-9f87-1d2c3b4a5e6f")
            @RequestParam(required = false) String accountId,
            @Parameter(description = "Start date in ISO 8601 date format (yyyy-MM-dd).", required = true, example = "2026-03-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date in ISO 8601 date format (yyyy-MM-dd).", required = true, example = "2026-03-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "IANA time zone used to interpret date boundaries.", example = "America/Sao_Paulo")
            @RequestHeader(value = "Time-Zone", defaultValue = "America/Sao_Paulo") String timeZone
    ) {
        ZoneId zoneId = resolveZoneId(timeZone);
        Instant fromInstant = fromDate.atStartOfDay(zoneId).toInstant();
        Instant toInstant = toDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant();
        GetDashboardMetricsResponse result = getDashboardMetricsUseCase.execute(accountId, fromInstant, toInstant);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/categories")
        @Operation(
            summary = "Get dashboard category totals",
            description = "Return income and expense totals grouped by category for the selected period and optional account filter."
        )
        @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "Dashboard category totals were retrieved successfully.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetDashboardCategoriesResponse.class))
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
    public ResponseEntity<GetDashboardCategoriesResponse> categories(
            @Parameter(description = "Optional account identifier to scope category totals to a single account.", example = "f4e3a2b1-6d5c-4b3a-9f87-1d2c3b4a5e6f")
            @RequestParam(required = false) String accountId,
            @Parameter(description = "Start date in ISO 8601 date format (yyyy-MM-dd).", required = true, example = "2026-03-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date in ISO 8601 date format (yyyy-MM-dd).", required = true, example = "2026-03-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "IANA time zone used to interpret date boundaries.", example = "America/Sao_Paulo")
            @RequestHeader(value = "Time-Zone", defaultValue = "America/Sao_Paulo") String timeZone
    ) {
        ZoneId zoneId = resolveZoneId(timeZone);
        Instant fromInstant = fromDate.atStartOfDay(zoneId).toInstant();
        Instant toInstant = toDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant();
        GetDashboardCategoriesResponse result = getDashboardCategoriesUseCase.execute(accountId, fromInstant, toInstant);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/cashflow")
        @Operation(
            summary = "Get dashboard cashflow timeline",
            description = "Return a continuous timeline of income and expenses with automatic granularity selection based on the date range length."
        )
        @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "Dashboard cashflow was retrieved successfully.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetDashboardCashflowResponse.class))
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
    public ResponseEntity<GetDashboardCashflowResponse> cashflow(
            @Parameter(description = "Optional account identifier to scope cashflow to a single account.", example = "f4e3a2b1-6d5c-4b3a-9f87-1d2c3b4a5e6f")
            @RequestParam(required = false) String accountId,
            @Parameter(description = "Start date in ISO 8601 date format (yyyy-MM-dd).", required = true, example = "2026-03-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date in ISO 8601 date format (yyyy-MM-dd).", required = true, example = "2026-03-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "IANA time zone used to interpret date boundaries.", example = "America/Sao_Paulo")
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
