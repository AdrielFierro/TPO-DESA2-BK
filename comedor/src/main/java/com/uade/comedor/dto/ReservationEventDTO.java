package com.uade.comedor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReservationEventDTO {
    private Long reservationId;
    private String userId;
    private Long locationId;
    private String mealTime;
    private String reservationTimeSlot;
    private LocalDateTime reservationDate;
    private String status;
    private BigDecimal cost;
    private LocalDateTime createdAt;
    private LocalTime slotStartTime;
    private LocalTime slotEndTime;

    // Constructors
    public ReservationEventDTO() {
    }

    public ReservationEventDTO(Long reservationId, String userId, Long locationId,
                               String mealTime, String reservationTimeSlot,
                               LocalDateTime reservationDate, String status,
                               BigDecimal cost, LocalDateTime createdAt,
                               LocalTime slotStartTime, LocalTime slotEndTime) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.locationId = locationId;
        this.mealTime = mealTime;
        this.reservationTimeSlot = reservationTimeSlot;
        this.reservationDate = reservationDate;
        this.status = status;
        this.cost = cost;
        this.createdAt = createdAt;
        this.slotStartTime = slotStartTime;
        this.slotEndTime = slotEndTime;
    }

    // Getters and Setters
    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getMealTime() {
        return mealTime;
    }

    public void setMealTime(String mealTime) {
        this.mealTime = mealTime;
    }

    public String getReservationTimeSlot() {
        return reservationTimeSlot;
    }

    public void setReservationTimeSlot(String reservationTimeSlot) {
        this.reservationTimeSlot = reservationTimeSlot;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public LocalTime getSlotStartTime() {
        return slotStartTime;
    }

    public void setSlotStartTime(LocalTime slotStartTime) {
        this.slotStartTime = slotStartTime;
    }

    public LocalTime getSlotEndTime() {
        return slotEndTime;
    }

    public void setSlotEndTime(LocalTime slotEndTime) {
        this.slotEndTime = slotEndTime;
    }
}
