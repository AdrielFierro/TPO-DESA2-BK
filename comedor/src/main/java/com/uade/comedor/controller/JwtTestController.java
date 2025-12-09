package com.uade.comedor.controller;

import com.uade.comedor.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de prueba para validar JWT
 */
@RestController
@RequestMapping("/test")
public class JwtTestController {

    private final JwtService jwtService;

    public JwtTestController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Endpoint de prueba para validar JWT y ver los claims
     */
    @PostMapping("/jwt/validate")
    public ResponseEntity<Map<String, Object>> validateJwt(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = jwtService.validateToken(token);
            response.put("valid", isValid);
            
            if (isValid) {
                response.put("userId", jwtService.extractUserId(token));
                response.put("email", jwtService.extractEmail(token));
                response.put("name", jwtService.extractName(token));
                response.put("role", jwtService.extractRole(token));
            }
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para extraer solo del header Authorization
     */
    @GetMapping("/jwt/info")
    public ResponseEntity<Map<String, Object>> getJwtInfo(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = authHeader.substring(7); // Quitar "Bearer "
            
            boolean isValid = jwtService.validateToken(token);
            response.put("valid", isValid);
            
            if (isValid) {
                response.put("userId", jwtService.extractUserId(token));
                response.put("email", jwtService.extractEmail(token));
                response.put("name", jwtService.extractName(token));
                response.put("role", jwtService.extractRole(token));
            }
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getClass().getName());
        }
        
        return ResponseEntity.ok(response);
    }
}
