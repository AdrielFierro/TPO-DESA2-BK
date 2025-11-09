package com.uade.comedor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long locationId; // Referencia a Location

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuMeal.MealTime mealTime; // DESAYUNO, ALMUERZO, MERIENDA, CENA

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private MealTimeSlot reservationTimeSlot; // Slot específico (nullable para slots dinámicos)

    @Column(nullable = false)
    private LocalDateTime reservationDate; // Fecha de la reserva (sin hora, la hora viene del slot)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private BigDecimal cost;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum ReservationStatus {
        ACTIVA, CONFIRMADA, CANCELADA, AUSENTE
    }

    // Getters and Setters
    public Long getId() {
        return reservationId;
    }

    public void setId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public MenuMeal.MealTime getMealTime() {
        return mealTime;
    }

    public void setMealTime(MenuMeal.MealTime mealTime) {
        this.mealTime = mealTime;
    }

    public MealTimeSlot getReservationTimeSlot() {
        return reservationTimeSlot;
    }

    public void setReservationTimeSlot(MealTimeSlot reservationTimeSlot) {
        this.reservationTimeSlot = reservationTimeSlot;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}