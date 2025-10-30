package com.uade.comedor.dto;

import com.uade.comedor.entity.MealTime;
import java.util.List;

public record MealRequest(
    MealTime mealTime,
    List<Long> products
) {}