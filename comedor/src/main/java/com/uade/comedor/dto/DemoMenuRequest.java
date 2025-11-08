package com.uade.comedor.dto;

import com.uade.comedor.entity.MenuDay;
import com.uade.comedor.entity.MenuMeal;

public class DemoMenuRequest {
    private MenuDay.DayOfWeek day;
    private MenuMeal.MealTime mealTime;

    public DemoMenuRequest() {
    }

    public DemoMenuRequest(MenuDay.DayOfWeek day, MenuMeal.MealTime mealTime) {
        this.day = day;
        this.mealTime = mealTime;
    }

    public MenuDay.DayOfWeek getDay() {
        return day;
    }

    public void setDay(MenuDay.DayOfWeek day) {
        this.day = day;
    }

    public MenuMeal.MealTime getMealTime() {
        return mealTime;
    }

    public void setMealTime(MenuMeal.MealTime mealTime) {
        this.mealTime = mealTime;
    }
}
