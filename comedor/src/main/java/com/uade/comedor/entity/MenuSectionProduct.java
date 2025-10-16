package com.uade.comedor.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_section_products")
public class MenuSectionProduct {
    @EmbeddedId
    private MenuSectionProductId id;

    @Column
    private Integer sortOrder;

    // Getters and Setters
    public MenuSectionProductId getId() {
        return id;
    }

    public void setId(MenuSectionProductId id) {
        this.id = id;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Embeddable
    public static class MenuSectionProductId {
        @Column(name = "menu_section_id")
        private Long menuSectionId;

        @Column(name = "product_id")
        private Long productId;

        // Getters and Setters
        public Long getMenuSectionId() {
            return menuSectionId;
        }

        public void setMenuSectionId(Long menuSectionId) {
            this.menuSectionId = menuSectionId;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }
    }
}