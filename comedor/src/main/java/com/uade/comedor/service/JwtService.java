package com.uade.comedor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;

/**
 * Servicio para manejar validación y extracción de información de JWT
 * NOTA: Por ahora NO valida la firma (modo desarrollo)
 */
@Service
public class JwtService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extrae el userId (sub) del JWT
     */
    public String extractUserId(String token) {
        Map<String, Object> claims = extractAllClaims(token);
        return (String) claims.get("sub"); // El "sub" contiene el userId
    }

    /**
     * Extrae el email del JWT
     */
    public String extractEmail(String token) {
        Map<String, Object> claims = extractAllClaims(token);
        return (String) claims.get("email");
    }

    /**
     * Extrae el nombre del JWT
     */
    public String extractName(String token) {
        Map<String, Object> claims = extractAllClaims(token);
        return (String) claims.get("name");
    }

    /**
     * Extrae el rol del JWT
     */
    public String extractRole(String token) {
        Map<String, Object> claims = extractAllClaims(token);
        return (String) claims.get("role");
    }

    /**
     * Extrae el subrol del JWT
     */
    public String extractSubrol(String token) {
        Map<String, Object> claims = extractAllClaims(token);
        return (String) claims.get("subrol");
    }

    /**
     * Extrae el wallet UUID del JWT
     * El wallet viene como un array, tomamos el primer elemento
     */
    @SuppressWarnings("unchecked")
    public String extractWalletId(String token) {
        Map<String, Object> claims = extractAllClaims(token);
        Object walletObj = claims.get("wallet");
        
        if (walletObj == null) {
            return null;
        }
        
        // El wallet viene como un array, tomamos el primer elemento
        if (walletObj instanceof java.util.List) {
            java.util.List<String> wallets = (java.util.List<String>) walletObj;
            return wallets.isEmpty() ? null : wallets.get(0);
        }
        
        // Si por alguna razón viene como String directo
        return walletObj.toString();
    }

    /**
     * Valida si el token es válido (sin verificar firma)
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrae todos los claims del token SIN VERIFICAR LA FIRMA
     * NOTA: Esto es inseguro y solo para desarrollo
     * Decodifica manualmente el payload del JWT (parte del medio)
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractAllClaims(String token) {
        try {
            // El JWT tiene 3 partes separadas por punto: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            // Decodificar el payload (segunda parte)
            String payload = parts[1];
            byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
            String decodedPayload = new String(decodedBytes);
            
            // Parsear JSON a Map
            return objectMapper.readValue(decodedPayload, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JWT token: " + e.getMessage(), e);
        }
    }
}
