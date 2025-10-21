package com.uade.comedor.dto;

import java.util.List;

public class MenuInputMeal {
    private String mealTime;
    private List<Long> products; // product IDs

    public String getMealTime() {
        return mealTime;
    }

    public void setMealTime(String mealTime) {
        this.mealTime = mealTime;
    }

    public List<Long> getProducts() {
        return products;
    }

    public void setProducts(List<Long> products) {
        this.products = products;
    }
}
