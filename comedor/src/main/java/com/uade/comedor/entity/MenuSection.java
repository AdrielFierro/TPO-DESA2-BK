package com.uade.comedor.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "menu_sections")
public class MenuSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "meal_block_id", nullable = false)
    private MealBlock mealBlock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SectionType sectionType;

    @ManyToMany
    @JoinTable(
        name = "menu_section_products",
        joinColumns = @JoinColumn(name = "section_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;

    public enum SectionType {
        PLATOS, BEBIDAS, POSTRES
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MealBlock getMealBlock() {
        return mealBlock;
    }

    public void setMealBlock(MealBlock mealBlock) {
        this.mealBlock = mealBlock;
    }

    public SectionType getSectionType() {
        return sectionType;
    }

    public void setSectionType(SectionType sectionType) {
        this.sectionType = sectionType;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}