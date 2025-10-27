package com.uade.comedor.dto;

import java.util.List;
import com.uade.comedor.entity.MenuDay.DayOfWeek;

public class MenuDayDTO {
    private DayOfWeek day;
    private List<MenuMealDTO> meals;

    // Getters y Setters
    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public List<MenuMealDTO> getMeals() {
        return meals;
    }

    public void setMeals(List<MenuMealDTO> meals) {
        this.meals = meals;
    }
}