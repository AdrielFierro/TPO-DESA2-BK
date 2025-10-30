package com.uade.comedor.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class MenuMeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private MealTime mealTime;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "menu_meal_products",
        joinColumns = @JoinColumn(name = "menu_meal_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;
    
    @ManyToOne
    @JoinColumn(name = "menu_day_id")
    private MenuDay menuDay;

    public enum MealTime {
        DESAYUNO, ALMUERZO, MERIENDA, CENA
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MealTime getMealTime() {
        return mealTime;
    }

    public void setMealTime(MealTime mealTime) {
        this.mealTime = mealTime;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public MenuDay getMenuDay() {
        return menuDay;
    }

    public void setMenuDay(MenuDay menuDay) {
        this.menuDay = menuDay;
    }
}