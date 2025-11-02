package com.uade.comedor.dto;

import com.uade.comedor.entity.MenuMeal;

import java.time.LocalTime;

public class MealTimeScheduleDTO {
    private MenuMeal.MealTime mealTime;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes;
    
    public MealTimeScheduleDTO() {}
    
    public MealTimeScheduleDTO(MenuMeal.MealTime mealTime, LocalTime startTime, LocalTime endTime, Integer slotDurationMinutes) {
        this.mealTime = mealTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
    }
    
    // Getters and Setters
    public MenuMeal.MealTime getMealTime() {
        return mealTime;
    }
    
    public void setMealTime(MenuMeal.MealTime mealTime) {
        this.mealTime = mealTime;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getSlotDurationMinutes() {
        return slotDurationMinutes;
    }
    
    public void setSlotDurationMinutes(Integer slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }
}
