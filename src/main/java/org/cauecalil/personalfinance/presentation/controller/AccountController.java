package org.cauecalil.personalfinance.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    @GetMapping
    public ResponseEntity<List<Object>> list() {
        var result = List.of();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
