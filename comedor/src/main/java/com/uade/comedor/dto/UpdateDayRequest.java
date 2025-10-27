package com.uade.comedor.dto;

import java.util.List;

public class UpdateDayRequest {
    private List<MenuMealCreateRequest> meals;

    // Getters y Setters
    public List<MenuMealCreateRequest> getMeals() {
        return meals;
    }

    public void setMeals(List<MenuMealCreateRequest> meals) {
        this.meals = meals;
    }
}