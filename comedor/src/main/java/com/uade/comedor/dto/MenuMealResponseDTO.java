package com.uade.comedor.dto;

import java.util.Map;
import java.util.List;

public class MenuMealResponseDTO {
    private String mealTime;
    private Map<String, List<ProductDTO>> sections;

    // Getters y Setters
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