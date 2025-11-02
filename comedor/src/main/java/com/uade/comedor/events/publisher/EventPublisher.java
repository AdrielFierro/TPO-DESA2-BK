package com.uade.comedor.events.publisher;

import com.uade.comedor.events.config.RabbitMQConfig;
import com.uade.comedor.events.dto.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio encargado de publicar eventos a RabbitMQ.
 * 
 * Este servicio es el "cartero" que toma los eventos que generas en tu código
 * y los envía a RabbitMQ para que otros sistemas puedan recibirlos.
 * 
 * Flujo de publicación:
 * 1. Tu código de negocio (ReservationService) crea un evento
 * 2. Llama a publishReservationUpdated() o publishBillCreated()
 * 3. Este servicio envuelve el evento en un EventEnvelope con metadata
 * 4. RabbitTemplate envía el mensaje al exchange con el routing key correcto
 * 5. RabbitMQ enruta el mensaje a la(s) cola(s) correspondiente(s)
 * 6. Otros servicios consumen el mensaje desde la cola
 */
@Service
public class EventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);
    
    private final RabbitTemplate rabbitTemplate;
    
    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    /**
     * Publica un evento de reserva actualizada.
     * 
     * @param payload El payload con los datos de la reserva
     * @param userId ID del usuario que realizó la acción
     * @param userRole Rol del usuario ("student" o "professor")
     */
    public <T> void publishReservationUpdated(T payload, String userId, String userRole) {
        EventEnvelope<T> envelope = buildEventEnvelope(
            "reservation.updated",
            payload,
            userId,
            userRole
        );
        
        try {
            // Envía el mensaje al exchange con el routing key
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.COMEDOR_EXCHANGE,                    // Exchange destino
                RabbitMQConfig.RESERVATION_UPDATED_ROUTING_KEY,     // Routing key
                envelope                                             // El mensaje (se convierte a JSON automáticamente)
            );
            
            logger.info("✅ Evento 'reservation.updated' publicado exitosamente. EventID: {}", envelope.getEventId());
            
        } catch (Exception e) {
            logger.error("❌ Error al publicar evento 'reservation.updated': {}", e.getMessage(), e);
            // En producción, podrías querer reintentar o guardar en una tabla de outbox
        }
    }
    
    /**
     * Publica un evento de factura creada.
     * 
     * @param payload El payload con los datos de la factura
     * @param userId ID del usuario que realizó la compra
     * @param userRole Rol del usuario
     */
    public <T> void publishBillCreated(T payload, String userId, String userRole) {
        EventEnvelope<T> envelope = buildEventEnvelope(
            "bill.created",
            payload,
            userId,
            userRole
        );
        
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.COMEDOR_EXCHANGE,
                RabbitMQConfig.BILL_CREATED_ROUTING_KEY,
                envelope
            );
            
            logger.info("✅ Evento 'bill.created' publicado exitosamente. EventID: {}", envelope.getEventId());
            
        } catch (Exception e) {
            logger.error("❌ Error al publicar evento 'bill.created': {}", e.getMessage(), e);
        }
    }
    
    /**
     * Construye el sobre del evento con toda la metadata requerida.
     * 
     * Este método llena todos los campos del EventEnvelope según el spec AsyncAPI:
     * - eventId: UUID único para este evento
     * - eventType: tipo de evento ("reservation.updated" o "bill.created")
     * - occurredAt: timestamp actual (cuándo ocurrió el evento)
     * - emittedAt: timestamp actual (cuándo se emitió a la cola)
     * - sourceModule: nombre de este módulo
     * - tenant: info de organización y campus
     * - actor: quién realizó la acción
     * - version: versión del schema del evento
     * - payload: los datos específicos del evento
     */
    private <T> EventEnvelope<T> buildEventEnvelope(String eventType, T payload, String userId, String userRole) {
        LocalDateTime now = LocalDateTime.now();
        
        return EventEnvelope.<T>builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(eventType)
            .occurredAt(now)
            .emittedAt(now)
            .sourceModule("cafeteria-service")  // Puedes cambiarlo a "comedor-service"
            .tenant(EventEnvelope.TenantInfo.builder()
                .orgId("UADE")
                .campusId("CENTRO")  // Esto podría venir de la Location
                .build())
            .actor(EventEnvelope.ActorInfo.builder()
                .userId(userId)
                .role(userRole)
                .build())
            .version("1.0")
            .payload(payload)
            .build();
    }
}
