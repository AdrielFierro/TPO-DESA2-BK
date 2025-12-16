package com.uade.comedor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.uade.comedor.repository.ReservationRepository;
import com.uade.comedor.entity.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tareas programadas para el mantenimiento automático de reservas
 */
@Component
public class ReservationScheduledTasks {
    
    private static final Logger logger = LoggerFactory.getLogger(ReservationScheduledTasks.class);
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private ReservationEventService reservationEventService;
    
    /**
     * Tarea que se ejecuta cada 30 minutos para marcar como AUSENTE
     * las reservas ACTIVAS cuya fecha ya pasó
     */
    @Scheduled(fixedRate = 1800000) // 30 minutos = 1,800,000 ms
    @Transactional
    public void markExpiredReservationsAsAbsent() {
        LocalDateTime now = LocalDateTime.now();
        
        // Buscar todas las reservas ACTIVAS cuya fecha ya pasó
        List<Reservation> expiredReservations = reservationRepository.findAll().stream()
            .filter(r -> r.getStatus() == Reservation.ReservationStatus.ACTIVA)
            .filter(r -> r.getReservationDate().isBefore(now))
            .toList();
        
        if (!expiredReservations.isEmpty()) {
            expiredReservations.forEach(reservation -> {
                reservation.setStatus(Reservation.ReservationStatus.AUSENTE);
                Reservation savedReservation = reservationRepository.save(reservation);
                
                // Publicar evento de reserva actualizada (marcada como ausente)
                reservationEventService.publishReservationUpdatedEvent(savedReservation);
            });
            
            logger.info("✅ Marcadas {} reservas vencidas como AUSENTE", expiredReservations.size());
        }
    }
    
    /**
     * Tarea que se ejecuta diariamente a las 3:00 AM para limpiar
     * reservas antiguas (CANCELADAS, AUSENTES) de más de 30 días
     * Esto es opcional, puedes comentarla si no la necesitas
     */
    // @Scheduled(cron = "0 0 3 * * ?") // A las 3:00 AM todos los días
    // @Transactional
    // public void cleanOldReservations() {
    //     LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
    //     // Buscar reservas CANCELADAS o AUSENTES de más de 30 días
    //     List<Reservation> oldReservations = reservationRepository.findAll().stream()
    //         .filter(r -> r.getStatus() == Reservation.ReservationStatus.CANCELADA || 
    //                     r.getStatus() == Reservation.ReservationStatus.AUSENTE)
    //         .filter(r -> r.getReservationDate().isBefore(thirtyDaysAgo))
    //         .toList();
        
    //     if (!oldReservations.isEmpty()) {
    //         logger.info("🧹 Limpiando {} reservas antiguas (>30 días)", oldReservations.size());
    //         reservationRepository.deleteAll(oldReservations);
    //         logger.info("✅ Limpieza completada: {} reservas eliminadas", oldReservations.size());
    //     } else {
    //         logger.debug("ℹ️ No hay reservas antiguas para limpiar");
    //     }
    // }
}
