package com.uade.comedor.dto;

import com.uade.comedor.entity.MenuMeal;
import java.time.LocalDateTime;

public class CreateReservationRequest {
    private Long userId;
    private Long locationId;
    private MenuMeal.MealTime mealTime; // DESAYUNO, ALMUERZO, MERIENDA, CENA
    private LocalDateTime reservationDate; // Fecha y hora específica de la reserva
    
    // Getters and Setters
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

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }
}