package com.uade.comedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.uade.comedor.entity.Reservation;
import com.uade.comedor.entity.MealTimeSlot;
import com.uade.comedor.entity.MenuMeal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    
    // Query para el sistema antiguo con MealTimeSlot
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.locationId = :locationId " +
           "AND r.reservationTimeSlot = :timeSlot " +
           "AND DATE(r.reservationDate) = DATE(:date) " +
           "AND r.status <> 'CANCELADA'")
    long countByLocationAndTimeSlot(@Param("locationId") Long locationId,
                                     @Param("timeSlot") MealTimeSlot timeSlot,
                                     @Param("date") LocalDateTime date);
    
    // Nuevo query para el sistema dinámico basado en horarios
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.locationId = :locationId " +
           "AND r.mealTime = :mealTime " +
           "AND r.reservationDate >= :startTime " +
           "AND r.reservationDate < :endTime " +
           "AND r.status <> 'CANCELADA'")
    long countByLocationIdAndMealTimeAndReservationDateBetween(
            @Param("locationId") Long locationId,
            @Param("mealTime") MenuMeal.MealTime mealTime,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    // Query para obtener reservas activas y recientes
    // Muestra: ACTIVA, CONFIRMADA (sin límite de tiempo)
    // Muestra: AUSENTE de los últimos 2 días
    // NO muestra: CANCELADA
    // Ordenadas: Primero ACTIVA (más temprana primero), luego CONFIRMADA (más reciente primero), luego AUSENTE (más reciente primero)
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId " +
           "AND (r.status = 'ACTIVA' OR r.status = 'CONFIRMADA' OR " +
           "(r.status = 'AUSENTE' AND r.reservationDate >= :twoDaysAgo)) " +
           "ORDER BY " +
           "CASE r.status " +
           "  WHEN 'ACTIVA' THEN 1 " +
           "  WHEN 'CONFIRMADA' THEN 2 " +
           "  WHEN 'AUSENTE' THEN 3 " +
           "  ELSE 4 " +
           "END, " +
           "CASE WHEN r.status = 'ACTIVA' THEN r.reservationDate END ASC, " +
           "CASE WHEN r.status <> 'ACTIVA' THEN r.reservationDate END DESC")
    List<Reservation> findActiveAndRecentByUserId(@Param("userId") Long userId, 
                                                   @Param("twoDaysAgo") LocalDateTime twoDaysAgo);
    
    // Query para obtener reservas de un usuario entre dos fechas
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId " +
           "AND r.reservationDate >= :startDate " +
           "AND r.reservationDate <= :endDate " +
           "ORDER BY r.reservationDate DESC")
    List<Reservation> findByUserIdAndDateBetween(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    // Query para obtener todas las reservas entre dos fechas
    @Query("SELECT r FROM Reservation r WHERE r.reservationDate >= :startDate " +
           "AND r.reservationDate <= :endDate " +
           "ORDER BY r.reservationDate DESC")
    List<Reservation> findByDateBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    // Query para verificar si un usuario ya tiene una reserva en un slot específico
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId " +
           "AND r.mealTime = :mealTime " +
           "AND r.reservationDate >= :startTime " +
           "AND r.reservationDate < :endTime " +
           "AND r.status <> 'CANCELADA'")
    long countByUserIdAndMealTimeAndReservationDateBetween(
            @Param("userId") Long userId,
            @Param("mealTime") MenuMeal.MealTime mealTime,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}