package com.uade.comedor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Envelope para eventos de RabbitMQ
 * Estructura estándar para todos los eventos del sistema
 */
public class EventEnvelope<T> {
    private String eventId;
    private String eventType;
    
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime occurredAt;
    
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime emittedAt;
    
    private String sourceModule;
    private T payload;

    // Constructor vacío
    public EventEnvelope() {
    }

    // Constructor completo
    public EventEnvelope(String eventType, LocalDateTime occurredAt, T payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.emittedAt = LocalDateTime.now();
        this.sourceModule = "Comedor";
        this.payload = payload;
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public LocalDateTime getEmittedAt() {
        return emittedAt;
    }

    public void setEmittedAt(LocalDateTime emittedAt) {
        this.emittedAt = emittedAt;
    }

    public String getSourceModule() {
        return sourceModule;
    }

    public void setSourceModule(String sourceModule) {
        this.sourceModule = sourceModule;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
