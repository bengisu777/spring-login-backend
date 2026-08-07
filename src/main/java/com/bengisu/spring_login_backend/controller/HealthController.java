package com.bengisu.spring_login_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    record HealthResponse(String status) {}

        @GetMapping("/api/health")
        public HealthResponse health() {
            return new HealthResponse("ok");
        }
    
}