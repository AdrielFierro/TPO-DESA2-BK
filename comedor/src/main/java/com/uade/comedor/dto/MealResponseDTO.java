package com.uade.comedor.dto;

import java.util.List;
import java.util.Map;

public class MealResponseDTO {
    private String mealTime;
    private Map<String, List<ProductResponseDTO>> sections;

    // Getters y Setters
    public String getMealTime() {
        return mealTime;
    }

    public void setMealTime(String mealTime) {
        this.mealTime = mealTime;
    }

    public Map<String, List<ProductResponseDTO>> getSections() {
        return sections;
    }

    public void setSections(Map<String, List<ProductResponseDTO>> sections) {
        this.sections = sections;
    }
}