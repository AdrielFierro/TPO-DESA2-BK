package com.uade.comedor.events.listener;

import com.uade.comedor.events.dto.EventEnvelope;
import com.uade.comedor.events.dto.ReservationUpdatedPayload;
import com.uade.comedor.events.dto.BillCreatedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * EventListener - Consumidor de eventos de RabbitMQ
 * 
 * Este componente "escucha" las colas de RabbitMQ y procesa los eventos
 * cuando llegan.
 * 
 * ¿Cómo funciona @RabbitListener?
 * 1. Spring se conecta a RabbitMQ
 * 2. Se suscribe a la cola especificada
 * 3. Cuando llega un mensaje, deserializa el JSON automáticamente
 * 4. Llama al método con el objeto ya parseado
 * 5. Si el método termina sin excepción, envía ACK a RabbitMQ
 * 6. Si el método lanza excepción, reintenta o envía a DLQ
 * 
 * Para desactivar este listener (si no quieres consumir eventos localmente),
 * simplemente comenta la anotación @Component.
 */
@Component  // ← Comenta esta línea si NO quieres consumir eventos localmente
public class EventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(EventListener.class);
    
    /**
     * Escucha eventos de reservas actualizadas.
     * 
     * Este método se ejecuta automáticamente cada vez que un mensaje
     * llega a la cola "comedor.reservation.updated".
     * 
     * Spring Boot deserializa el JSON a EventEnvelope<ReservationUpdatedPayload>
     * automáticamente gracias a Jackson.
     * 
     * @param event El evento completo (envelope + payload)
     */
    @RabbitListener(queues = "comedor.reservation.updated")
    public void handleReservationUpdated(EventEnvelope<ReservationUpdatedPayload> event) {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("🔔 EVENTO RECIBIDO: {}", event.getEventType());
        logger.info("═══════════════════════════════════════════════════════");
        
        logger.info("📋 Event ID: {}", event.getEventId());
        logger.info("⏰ Occurred At: {}", event.getOccurredAt());
        logger.info("📡 Emitted At: {}", event.getEmittedAt());
        logger.info("🏢 Organization: {}", event.getTenant().getOrgId());
        logger.info("🏫 Campus: {}", event.getTenant().getCampusId());
        logger.info("👤 User: {} ({})", event.getActor().getUserId(), event.getActor().getRole());
        
        logger.info("-----------------------------------------------------------");
        logger.info("📦 PAYLOAD:");
        
        ReservationUpdatedPayload payload = event.getPayload();
        
        logger.info("   🆔 Reservation ID: {}", payload.getReservationId());
        logger.info("   🎬 Action: {}", payload.getAction());
        logger.info("   📍 Location: {}", payload.getLocationId());
        logger.info("   🕐 Start: {}", payload.getStartDateTime());
        logger.info("   🕑 End: {}", payload.getEndDateTime());
        logger.info("   📝 Description: {}", payload.getDescription());
        
        logger.info("-----------------------------------------------------------");
        
        // 🎯 Aquí puedes agregar tu lógica de negocio según la acción:
        switch (payload.getAction()) {
            case created:
                logger.info("✨ ACCIÓN: Nueva reserva creada");
                // TODO: Enviar email de confirmación al usuario
                // TODO: Crear evento en calendario
                // TODO: Actualizar estadísticas de ocupación
                sendConfirmationEmail(event.getActor().getUserId(), payload);
                break;
                
            case confirmed:
                logger.info("✅ ACCIÓN: Reserva confirmada por el usuario");
                // TODO: Enviar recordatorio 1 hora antes
                // TODO: Notificar a cocina
                scheduleReminder(payload);
                break;
                
            case cancelled:
                logger.info("❌ ACCIÓN: Reserva cancelada");
                // TODO: Enviar email de cancelación
                // TODO: Liberar capacidad
                // TODO: Actualizar métricas
                updateCapacity(payload);
                break;
                
            case updated:
                logger.info("📝 ACCIÓN: Reserva modificada");
                // TODO: Notificar cambio al usuario
                break;
                
            case deleted:
                logger.info("🗑️ ACCIÓN: Reserva eliminada del sistema");
                // TODO: Limpieza de datos relacionados
                break;
        }
        
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("✅ Evento procesado exitosamente");
        logger.info("═══════════════════════════════════════════════════════\n");
    }
    
    /**
     * Escucha eventos de facturas creadas.
     * 
     * Se ejecuta cada vez que se genera una nueva factura.
     */
    @RabbitListener(queues = "comedor.bill.created")
    public void handleBillCreated(EventEnvelope<BillCreatedPayload> event) {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("🔔 EVENTO RECIBIDO: {}", event.getEventType());
        logger.info("═══════════════════════════════════════════════════════");
        
        logger.info("📋 Event ID: {}", event.getEventId());
        logger.info("⏰ Occurred At: {}", event.getOccurredAt());
        logger.info("👤 User: {} ({})", event.getActor().getUserId(), event.getActor().getRole());
        
        logger.info("-----------------------------------------------------------");
        logger.info("📦 PAYLOAD:");
        
        BillCreatedPayload payload = event.getPayload();
        
        logger.info("   🆔 Bill ID: {}", payload.getId());
        logger.info("   📅 Date: {}", payload.getDate());
        logger.info("   💰 Subtotal: ${}", payload.getSubtotal());
        logger.info("   🎫 Reservation ID: {}", 
            payload.getReservationId() != null ? payload.getReservationId() : "N/A");
        logger.info("   📦 Products: {} items", payload.getProducts().size());
        
        logger.info("   ------- Productos -------");
        payload.getProducts().forEach(product -> {
            logger.info("      • {} - ${} ({})", 
                product.getName(), 
                product.getPrice(), 
                product.getProductType());
        });
        
        logger.info("-----------------------------------------------------------");
        
        // 🎯 Lógica de negocio para facturas:
        logger.info("💼 ACCIONES:");
        // TODO: Registrar en sistema contable
        registerInAccounting(payload);
        
        // TODO: Actualizar inventario
        updateInventory(payload);
        
        // TODO: Generar reporte de ventas
        generateSalesReport(payload);
        
        // TODO: Si hay reserva asociada, vincular
        if (payload.getReservationId() != null) {
            logger.info("   🔗 Vinculando factura con reserva {}", payload.getReservationId());
        }
        
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("✅ Evento procesado exitosamente");
        logger.info("═══════════════════════════════════════════════════════\n");
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // Métodos helper (stubs - implementar según tu lógica de negocio)
    // ═══════════════════════════════════════════════════════════════════
    
    private void sendConfirmationEmail(String userId, ReservationUpdatedPayload payload) {
        logger.info("   📧 Enviando email de confirmación a usuario {}", userId);
        // TODO: Integrar con servicio de emails (ej: SendGrid, AWS SES)
    }
    
    private void scheduleReminder(ReservationUpdatedPayload payload) {
        logger.info("   ⏰ Programando recordatorio 1 hora antes de {}", payload.getStartDateTime());
        // TODO: Usar scheduler (Spring @Scheduled o Quartz)
    }
    
    private void updateCapacity(ReservationUpdatedPayload payload) {
        logger.info("   📊 Liberando capacidad para {} en {}", 
            payload.getStartDateTime(), 
            payload.getLocationId());
    }
    
    private void registerInAccounting(BillCreatedPayload payload) {
        logger.info("   💼 Registrando factura {} en sistema contable", payload.getId());
        // TODO: Integrar con sistema contable
    }
    
    private void updateInventory(BillCreatedPayload payload) {
        logger.info("   📦 Actualizando inventario ({} productos)", payload.getProducts().size());
        // TODO: Decrementar stock de productos vendidos
        payload.getProducts().forEach(product -> {
            logger.info("      ⬇️ Producto {} vendido", product.getName());
        });
    }
    
    private void generateSalesReport(BillCreatedPayload payload) {
        logger.info("   📊 Actualizando reporte de ventas (${} total)", payload.getSubtotal());
        // TODO: Agregar a métricas de ventas del día
    }
}
