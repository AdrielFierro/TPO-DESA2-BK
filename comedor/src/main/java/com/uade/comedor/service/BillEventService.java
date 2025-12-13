package com.uade.comedor.service;

import com.uade.comedor.dto.BillEventDTO;
import com.uade.comedor.dto.BillEventDTO.ProductEventDTO;
import com.uade.comedor.dto.EventEnvelope;
import com.uade.comedor.entity.Bill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class BillEventService {

    private static final Logger logger = LoggerFactory.getLogger(BillEventService.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.bill}")
    private String billExchange;

    public BillEventService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBillCreatedEvent(Bill bill) {
        try {
            BillEventDTO eventDTO = convertToEventDTO(bill);
            
            // Crear el envelope con el evento
            EventEnvelope<BillEventDTO> envelope = new EventEnvelope<>(
                "bill.created",
                bill.getCreatedAt(),
                eventDTO
            );
            
            // Enviar el envelope con routing key "bill.created"
            rabbitTemplate.convertAndSend(billExchange, "bill.created", envelope);
            
            logger.info("Bill created event published successfully for bill ID: {} with eventId: {}", 
                bill.getId(), envelope.getEventId());
        } catch (Exception e) {
            logger.error("Error publishing bill created event for bill ID: {}", bill.getId(), e);
            // No lanzamos la excepción para no afectar el flujo principal
        }
    }

    private BillEventDTO convertToEventDTO(Bill bill) {
        var products = bill.getProducts().stream()
                .map(product -> new ProductEventDTO(
                        product.getId(),
                        product.getName(),
                        product.getPrice()
                ))
                .collect(Collectors.toList());

        return new BillEventDTO(
                bill.getId(),
                bill.getUserId(),
                bill.getCartId(),
                bill.getReservationId(),
                bill.getSubtotal(),
                bill.getTotalWithDiscount(),
                bill.getTotalWithoutDiscount(),
                bill.getCreatedAt(),
                products
        );
    }
}
