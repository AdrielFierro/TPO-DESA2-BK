package com.uade.comedor.repository;

import com.uade.comedor.entity.Menu;
import com.uade.comedor.entity.MenuDay;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    
    @EntityGraph(attributePaths = {"days", "days.meals", "days.meals.products"})
    Optional<Menu> findByDays_Day(MenuDay.DayOfWeek day);
    
    boolean existsByDays_Day(MenuDay.DayOfWeek day);
    
    @Modifying
    @Transactional
    void deleteByDays_Day(MenuDay.DayOfWeek day);

    @EntityGraph(attributePaths = {"days", "days.meals", "days.meals.products"})
    Optional<Menu> findTopByOrderByLastModifiedDesc();

}