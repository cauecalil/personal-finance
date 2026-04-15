package com.cauecalil.personalfinance.presentation.controller;

import com.cauecalil.personalfinance.presentation.browser.HeartbeatBrowserLifecycle;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/heartbeat")
@RequiredArgsConstructor
public class HeartbeatController {
    private final HeartbeatBrowserLifecycle heartbeatBrowserLifecycle;

    @PostMapping
    public ResponseEntity<Void> heartbeat() {
        heartbeatBrowserLifecycle.markAlive();
        return ResponseEntity.noContent().build();
    }
}
