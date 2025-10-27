package com.uade.comedor.dto;

import java.util.List;

public class MenuDayResponseDTO {
    private String day;
    private List<MenuMealResponseDTO> meals;

    // Getters y Setters
    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public List<MenuMealResponseDTO> getMeals() {
        return meals;
    }

    public void setMeals(List<MenuMealResponseDTO> meals) {
        this.meals = meals;
    }
}