package com.uade.comedor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class WalletDTO {
    private String uuid;
    
    @JsonProperty("user_uuid")
    private String userUuid;
    
    private String currency;
    private BigDecimal balance;
    private String status;
    
    @JsonProperty("last_activity_at")
    private String lastActivityAt;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;

    // Constructors
    public WalletDTO() {
    }

    // Getters and Setters
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(String lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
