package com.uade.comedor.dto;

import java.util.List;
import com.uade.comedor.entity.MenuMeal.MealTime;

public class MenuMealDTO {
    private MealTime mealTime;
    private List<Long> productIds;

    // Getters y Setters
    public MealTime getMealTime() {
        return mealTime;
    }

    public void setMealTime(MealTime mealTime) {
        this.mealTime = mealTime;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}