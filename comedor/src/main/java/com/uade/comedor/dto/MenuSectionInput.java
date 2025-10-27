package com.uade.comedor.dto;

import java.util.List;

public class MenuSectionInput {
    private String sectionType;
    private List<Long> productIds;

    public String getSectionType() {
        return sectionType;
    }

    public void setSectionType(String sectionType) {
        this.sectionType = sectionType;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}