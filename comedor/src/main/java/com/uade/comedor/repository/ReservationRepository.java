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
}