package com.uade.comedor.dto;

import java.util.List;

public class WalletApiResponse {
    private boolean success;
    private List<WalletDTO> data;

    // Constructors
    public WalletApiResponse() {
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<WalletDTO> getData() {
        return data;
    }

    public void setData(List<WalletDTO> data) {
        this.data = data;
    }
}
