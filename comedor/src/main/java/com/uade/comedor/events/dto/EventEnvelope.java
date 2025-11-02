package com.uade.comedor.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * EventEnvelope es el "sobre" que envuelve todos los eventos.
 * Contiene metadata común a todos los eventos: quién, cuándo, dónde, etc.
 * 
 * Según AsyncAPI spec: EventEnvelopeBase
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope<T> {
    
    // Identificador único del evento
    private String eventId;
    
    // Tipo de evento: "reservation.updated" o "bill.created"
    private String eventType;
    
    // Cuándo ocurrió el evento en el sistema (ej: cuando se guardó en BD)
    private LocalDateTime occurredAt;
    
    // Cuándo se emitió el evento a la cola (puede ser milisegundos después)
    private LocalDateTime emittedAt;
    
    // Módulo que emitió el evento
    private String sourceModule;
    
    // Información del tenant (organización y campus)
    private TenantInfo tenant;
    
    // Quién realizó la acción
    private ActorInfo actor;
    
    // Versión del esquema del evento
    private String version;
    
    // El payload específico del evento (ReservationUpdatedPayload o BillCreatedPayload)
    private T payload;
    
    /**
     * Información del tenant: a qué organización y campus pertenece
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantInfo {
        private String orgId;      // Ej: "UADE"
        private String campusId;   // Ej: "CENTRO"
    }
    
    /**
     * Información del actor: quién realizó la acción
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActorInfo {
        private String userId;
        private String role;  // "student" o "professor"
    }
}
