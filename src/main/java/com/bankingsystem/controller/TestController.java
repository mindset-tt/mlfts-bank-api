package com.bankingsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Test controller for debugging purposes.
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "timestamp", System.currentTimeMillis(),
            "message", "Banking System is running"
        ));
    }
}
