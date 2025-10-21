package com.uade.comedor.dto;

import com.uade.comedor.entity.Cart;
import java.util.List;

public class CartCreateRequest {
    private Cart.PaymentMethod paymentMethod;
    private List<Long> cart;

    // Getters and Setters
    public Cart.PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Cart.PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<Long> getCart() {
        return cart;
    }

    public void setCart(List<Long> cart) {
        this.cart = cart;
    }
}