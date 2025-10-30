package com.uade.comedor.dto;

import java.util.List;

public class UpdateMealRequest {
    private List<Long> productIds;

    // Getters y Setters
    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}