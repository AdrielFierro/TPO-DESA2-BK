package com.uade.comedor.dto;

public class MealShiftDTO {
    private String mealTime;
    private String schedule;

    public MealShiftDTO(String mealTime, String schedule) {
        this.mealTime = mealTime;
        this.schedule = schedule;
    }

    public String getMealTime() {
        return mealTime;
    }

    public void setMealTime(String mealTime) {
        this.mealTime = mealTime;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
}
