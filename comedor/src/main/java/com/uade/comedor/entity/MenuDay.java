package com.uade.comedor.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class MenuDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private DayOfWeek day;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "menuDay", fetch = FetchType.EAGER)
    private List<MenuMeal> meals;
    
    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    public enum DayOfWeek {
        LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public List<MenuMeal> getMeals() {
        return meals;
    }

    public void setMeals(List<MenuMeal> meals) {
        this.meals = meals;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }
}