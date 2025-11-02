package com.uade.comedor.repository;

import com.uade.comedor.entity.MealTimeSchedule;
import com.uade.comedor.entity.MenuMeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MealTimeScheduleRepository extends JpaRepository<MealTimeSchedule, Long> {
    Optional<MealTimeSchedule> findByMealTime(MenuMeal.MealTime mealTime);
}
