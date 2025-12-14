package com.uade.comedor.service;

import com.uade.comedor.dto.EventEnvelope;
import com.uade.comedor.dto.ReservationEventDTO;
import com.uade.comedor.entity.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReservationEventService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationEventService.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.reservation}")
    private String reservationExchange;

    public ReservationEventService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishReservationCreatedEvent(Reservation reservation) {
        try {
            ReservationEventDTO eventDTO = convertToEventDTO(reservation);
            
            // Crear el envelope con el evento
            EventEnvelope<ReservationEventDTO> envelope = new EventEnvelope<>(
                "reservation.created",
                reservation.getCreatedAt(),
                eventDTO
            );
            
            // Enviar el envelope con routing key "reservation.created"
            rabbitTemplate.convertAndSend(reservationExchange, "reservation.created", envelope);
            
            logger.info("Reservation created event published successfully for reservation ID: {} with eventId: {}", 
                reservation.getId(), envelope.getEventId());
        } catch (Exception e) {
            logger.error("Error publishing reservation created event for reservation ID: {}", reservation.getId(), e);
            // No lanzamos la excepción para no afectar el flujo principal
        }
    }

    public void publishReservationUpdatedEvent(Reservation reservation) {
        try {
            ReservationEventDTO eventDTO = convertToEventDTO(reservation);
            
            // Crear el envelope con el evento
            EventEnvelope<ReservationEventDTO> envelope = new EventEnvelope<>(
                "reservation.updated",
                java.time.LocalDateTime.now(),
                eventDTO
            );
            
            // Enviar el envelope con routing key "reservation.updated"
            rabbitTemplate.convertAndSend(reservationExchange, "reservation.updated", envelope);
            
            logger.info("Reservation updated event published successfully for reservation ID: {} with eventId: {}", 
                reservation.getId(), envelope.getEventId());
        } catch (Exception e) {
            logger.error("Error publishing reservation updated event for reservation ID: {}", reservation.getId(), e);
            // No lanzamos la excepción para no afectar el flujo principal
        }
    }

    private ReservationEventDTO convertToEventDTO(Reservation reservation) {
        return new ReservationEventDTO(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getLocationId(),
                reservation.getMealTime() != null ? reservation.getMealTime().name() : null,
                reservation.getReservationTimeSlot() != null ? reservation.getReservationTimeSlot().name() : null,
                reservation.getReservationDate(),
                reservation.getStatus() != null ? reservation.getStatus().name() : null,
                reservation.getCost(),
                reservation.getCreatedAt(),
                reservation.getSlotStartTime(),
                reservation.getSlotEndTime()
        );
    }
}
