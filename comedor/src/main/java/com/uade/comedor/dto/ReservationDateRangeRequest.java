package com.uade.comedor.dto;

import java.time.LocalDateTime;

public class ReservationDateRangeRequest {
    private String userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Constructors
    public ReservationDateRangeRequest() {
    }

    public ReservationDateRangeRequest(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
