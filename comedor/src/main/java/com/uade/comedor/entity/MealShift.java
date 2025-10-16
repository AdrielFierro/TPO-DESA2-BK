package com.uade.comedor.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "meal_shifts")
public class MealShift {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_time")
    private Meal.MealTime mealTime;

    @Column(nullable = false)
    private String schedule;

    // Getters and Setters
    public Meal.MealTime getMealTime() {
        return mealTime;
    }

    public void setMealTime(Meal.MealTime mealTime) {
        this.mealTime = mealTime;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
}