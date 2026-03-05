package org.cauecalil.personalfinance.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {
    @PostMapping
    public ResponseEntity<Object> sync(@RequestParam(defaultValue = "false") boolean fullSync) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
