package com.uade.comedor.dto;

import java.util.Map;
import java.util.List;

public class MealBlockDTO {
    private Long id;
    private String mealTime;
    private Map<String, List<ProductDTO>> sections;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMealTime() {
        return mealTime;
    }

    public void setMealTime(String mealTime) {
        this.mealTime = mealTime;
    }

    public Map<String, List<ProductDTO>> getSections() {
        return sections;
    }

    public void setSections(Map<String, List<ProductDTO>> sections) {
        this.sections = sections;
    }
}