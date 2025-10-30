package com.uade.comedor.dto;

import java.util.List;

public class MenuDayCreateRequest {
    private String day;
    private List<MenuMealCreateRequest> meals;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public List<MenuMealCreateRequest> getMeals() {
        return meals;
    }

    public void setMeals(List<MenuMealCreateRequest> meals) {
        this.meals = meals;
    }
}