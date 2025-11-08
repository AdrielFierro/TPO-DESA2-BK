package com.uade.comedor.dto;

import java.time.LocalDateTime;

public class ReservationDateRangeRequest {
    private Long userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Constructors
    public ReservationDateRangeRequest() {
    }

    public ReservationDateRangeRequest(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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
