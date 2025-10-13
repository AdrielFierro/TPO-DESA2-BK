package com.uade.comedor.dto;

// adapta los nombres si tu IdP devuelve otros campos
public record TokenResponse(
    String access_token,
    String token_type,
    Long expires_in,
    String refresh_token,
    String id_token // opcional
) {}
