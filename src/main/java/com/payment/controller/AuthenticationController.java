package com.payment.controller;

import com.payment.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/api/authenticate")
@AllArgsConstructor
public class AuthenticationController {
    private final AuthService authService;

    @PostMapping("/{merchantId}")
    public ResponseEntity<Map<String, String>> authenticateMerchant(@PathVariable Long merchantId) {
        String token = authService.generateToken(merchantId);
        return new ResponseEntity<>(Map.of("token", token), HttpStatus.OK);
    }
}
