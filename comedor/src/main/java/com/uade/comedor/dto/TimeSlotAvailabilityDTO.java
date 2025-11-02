package com.uade.comedor.dto;

import java.time.LocalTime;

public class TimeSlotAvailabilityDTO {
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer availableSeats;
    private Integer totalCapacity;
    private Boolean available;
    
    public TimeSlotAvailabilityDTO() {}
    
    public TimeSlotAvailabilityDTO(LocalTime startTime, LocalTime endTime, Integer availableSeats, Integer totalCapacity) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.availableSeats = availableSeats;
        this.totalCapacity = totalCapacity;
        this.available = availableSeats > 0;
    }
    
    // Getters and Setters
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getAvailableSeats() {
        return availableSeats;
    }
    
    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }
    
    public Integer getTotalCapacity() {
        return totalCapacity;
    }
    
    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }
    
    public Boolean getAvailable() {
        return available;
    }
    
    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
