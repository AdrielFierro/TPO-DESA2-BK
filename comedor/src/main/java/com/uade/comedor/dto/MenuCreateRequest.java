package com.uade.comedor.dto;

import java.util.List;

public class MenuCreateRequest {
    private String day;
    private List<MenuInputMeal> meals;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public List<MenuInputMeal> getMeals() {
        return meals;
    }

    public void setMeals(List<MenuInputMeal> meals) {
        this.meals = meals;
    }
}
