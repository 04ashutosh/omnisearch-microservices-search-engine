package com.omnisearch.apigateway.controller;

import com.omnisearch.apigateway.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestParam String username, @RequestParam String password) {
        // MOCK DATABASE AUTHENTICATION
        if ("admin".equals(username) && "admin123".equals(password)) {
            String signedToken = jwtUtil.generateToken(username);
            return Mono.just(ResponseEntity.ok(signedToken));
        }

        // Deny login
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials"));
    }
}
