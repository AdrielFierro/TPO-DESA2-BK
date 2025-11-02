package com.uade.comedor.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payload del evento "reservation.updated"
 * 
 * Contiene la información específica de una reserva que fue:
 * - created: creada
 * - updated: modificada
 * - confirmed: confirmada
 * - cancelled: cancelada
 * - deleted: eliminada
 * 
 * Según AsyncAPI spec: ReservationUpdatedPayload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationUpdatedPayload {
    
    // ID de la reserva
    private String reservationId;
    
    // Fecha/hora de inicio de la reserva (cuando comienza el slot)
    private LocalDateTime startDateTime;
    
    // Fecha/hora de fin de la reserva (cuando termina el slot)
    private LocalDateTime endDateTime;
    
    // Título descriptivo del evento
    private String title;
    
    // Descripción del evento
    private String description;
    
    // Qué acción se realizó sobre la reserva
    private ReservationAction action;
    
    // En qué ubicación/sede se hizo la reserva
    private String locationId;
    
    public enum ReservationAction {
        created,
        updated,
        confirmed,
        cancelled,
        deleted
    }
}
