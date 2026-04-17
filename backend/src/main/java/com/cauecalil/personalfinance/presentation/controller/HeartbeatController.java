package com.cauecalil.personalfinance.presentation.controller;

import com.cauecalil.personalfinance.presentation.browser.HeartbeatBrowserLifecycle;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/heartbeat")
@RequiredArgsConstructor
@Tag(name = "Heartbeat", description = "Operations for signaling desktop application liveness.")
public class HeartbeatController {
    private final HeartbeatBrowserLifecycle heartbeatBrowserLifecycle;

    @PostMapping
    @Operation(
        summary = "Send heartbeat signal",
        description = "Mark the desktop client as alive so background lifecycle controls can keep the local session active."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Heartbeat was registered successfully."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Request validation failed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "An unexpected server error occurred.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    public ResponseEntity<Void> heartbeat() {
        heartbeatBrowserLifecycle.markAlive();
        return ResponseEntity.noContent().build();
    }
}
