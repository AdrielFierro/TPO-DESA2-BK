package com.uade.comedor.dto;

import com.uade.comedor.entity.MenuMeal;
import java.time.LocalDateTime;

public class CreateReservationRequest {
    private String userId;
    private String locationId; // UUID from backoffice
    private MenuMeal.MealTime mealTime; // DESAYUNO, ALMUERZO, MERIENDA, CENA
    private LocalDateTime reservationDate; // Fecha y hora específica de la reserva
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
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