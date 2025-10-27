package com.uade.comedor.dto;

import java.util.List;

public class MenuMealCreateRequest {
    private String mealTime;
    private List<Long> productIds;

    public String getMealTime() {
        return mealTime;
    }

    public void setMealTime(String mealTime) {
        this.mealTime = mealTime;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}