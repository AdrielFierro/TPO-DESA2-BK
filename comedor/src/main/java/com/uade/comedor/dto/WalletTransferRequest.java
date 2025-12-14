package com.uade.comedor.dto;

import java.math.BigDecimal;

/**
 * DTO para solicitar una transferencia a la API de Wallet
 */
public class WalletTransferRequest {
    
    private String from;
    private String to;
    private String currency;
    private BigDecimal amount;
    private String type;
    private String description;

    public WalletTransferRequest() {
    }

    public WalletTransferRequest(String from, String to, String currency, BigDecimal amount, String type, String description) {
        this.from = from;
        this.to = to;
        this.currency = currency;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    // Getters y Setters
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
