package com.uade.comedor.dto;

import com.uade.comedor.entity.MealTimeSlot;

public class TimeSlotAvailabilityResponse {
    private MealTimeSlot timeSlot;
    private int availableSeats;
    private int totalCapacity;
    private boolean isAvailable;

    public TimeSlotAvailabilityResponse(MealTimeSlot timeSlot, int availableSeats, int totalCapacity) {
        this.timeSlot = timeSlot;
        this.availableSeats = availableSeats;
        this.totalCapacity = totalCapacity;
        this.isAvailable = availableSeats > 0;
    }

    // Getters and Setters
    public MealTimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(MealTimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
