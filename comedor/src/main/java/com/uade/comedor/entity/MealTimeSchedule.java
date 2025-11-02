package com.uade.comedor.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "meal_time_schedules")
public class MealTimeSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private MenuMeal.MealTime mealTime;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @Column(nullable = false)
    private Integer slotDurationMinutes; // Duración de cada slot en minutos (ej: 60)
    
    // Constructors
    public MealTimeSchedule() {}
    
    public MealTimeSchedule(MenuMeal.MealTime mealTime, LocalTime startTime, LocalTime endTime, Integer slotDurationMinutes) {
        this.mealTime = mealTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
